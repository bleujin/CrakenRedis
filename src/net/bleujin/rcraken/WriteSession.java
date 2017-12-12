package net.bleujin.rcraken;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections.list.SetUniqueList;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RListMultimap;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetMultimapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryUpdatedListener;
import org.redisson.api.map.event.MapEntryListener;

import net.bleujin.rcraken.Property.PType;
import net.bleujin.rcraken.def.Defined;
import net.bleujin.rcraken.extend.IndexEvent;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.SetUtil;
import net.ion.nsearcher.common.WriteDocument;
import net.ion.nsearcher.index.IndexJob;
import net.ion.nsearcher.index.IndexSession;
import net.ion.nsearcher.index.Indexer;

public class WriteSession {

	private Workspace wspace;
	private RedissonClient rclient;
	private ReadSession rsession;
	private Map<String, Object> attrs = MapUtil.newMap();
	private RMap<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	WriteSession(Workspace wspace, ReadSession rsession, RedissonClient rclient) {
		this.wspace = wspace;
		this.rsession = rsession;
		this.rclient = rclient ;
		this.dataMap = rclient.getMapCache(wspace.nodeMapName(), wspace.mapOption());
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName());
	}

	public ReadSession readSession() {
		return rsession;
	}

	public WriteNode pathBy(String path) {
		Fqn fqn = Fqn.from(path);

		return pathBy(fqn);
	}

	public WriteNode pathBy(Fqn fqn) {
		return new WriteNode(this, fqn, ObjectUtil.coalesce(JsonObject.fromString(dataMap.get(fqn.absPath())), new JsonObject()));
	}

	void merge(WriteNode wnode, Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {
			if (!dataMap.containsKey(current.absPath())) {
				struMap.put(current.getParent().absPath(), current.name());
				dataMap.put(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		dataMap.put(fqn.absPath(), data.toString());
		
		// handle lob
		childJson(data).filter(p -> "Lob".equals(p.asString("type"))&& hasAttribute(p.asString("value"))).forEach(p -> {
			if (attrs.get(p.asString("value")) instanceof InputStream) {
		        try {
		        	InputStream input = (InputStream) attrs.get(p.asString("value")) ;
		        	OutputStream output = wspace.outputStream(p.asString("value"));
					IOUtil.copyNClose(input, output);
				} catch (IOException ex) {
					attrs.put(p.asString("value"), ex.getMessage()) ;
				}
			}
		});
		
	}
	
	void removeChild(WriteNode wnode, Fqn fqn, JsonObject data) {
		Set<String> rs = SetUtil.newSet() ;
		decendant(fqn, rs); 
		
		dataMap.fastRemove(rs.toArray(new String[0])) ;
		struMap.fastRemove(rs.toArray(new String[0])) ;

		rclient.getKeys().findKeysByPattern(rsession.workspace().lobPrefix() + fqn.absPath() + "/*").forEach(key ->{
			rclient.getBinaryStream(key).delete() ;
		});
	}

	void removeSelf(WriteNode wnode, Fqn fqn, JsonObject data) {
		Set<String> rs = SetUtil.newSet() ;
		decendant(fqn, rs);
		rs.add(fqn.absPath()) ;
		
		dataMap.fastRemove(rs.toArray(new String[0])) ;
		struMap.fastRemove(rs.toArray(new String[0])) ;
		Fqn parent = fqn.getParent() ;
		if(! fqn.isRoot()) struMap.get(parent.absPath()).remove(fqn.name()) ;
		
		// remove lob
		rclient.getKeys().findKeysByPattern(rsession.workspace().lobPrefix() + fqn.absPath() + "$*").forEach(key ->{
			rclient.getBinaryStream(key).delete() ;
		});
		rclient.getKeys().findKeysByPattern(rsession.workspace().lobPrefix() + fqn.absPath() + "/*").forEach(key ->{
			rclient.getBinaryStream(key).delete() ;
		});
	}

	void decendant(Fqn parent, Set<String> rs) {
		Set<String> children = struMap.getAll(parent.absPath()) ;
		for (String child : children) {
			Fqn childFqn = Fqn.from(parent, child);
			rs.add(childFqn.absPath()) ;
			decendant(childFqn, rs);
		}
	}
	
	private Stream<JsonObject> childJson(final JsonObject parent){
		Iterator<String> keyIter = parent.keySet().iterator() ;
		return StreamSupport.stream(Spliterators.spliterator(new Iterator<JsonObject>() {
			public boolean hasNext() {
				return keyIter.hasNext();
			}
			public JsonObject next() {
				return parent.asJsonObject(keyIter.next());
			}
		}, parent.keySet().size(), 0), false);
	}
	
	private boolean hasAttribute(String name) {
		return attrs.containsKey(name) ;
	}

	public <T> void attribute(Class<T> clz, T obj) {
		attrs.put(clz.getCanonicalName(), obj);
	}

	public <T> T attribute(Class<T> clz) {
		return (T) attrs.get(clz.getCanonicalName());
	}

	public void endTran() {
		if (workspace().central() != null) { 
			List<IndexEvent> ievents = (List<IndexEvent>) attrs.get(workspace().indexListenerId()) ;
			Indexer indexer = workspace().central().newIndexer() ;
			indexer.index(isession -> {
				for (IndexEvent ie : ievents) {
					if (ie.eventType() == EventType.REMOVED) {
						isession.deleteById(ie.fqn().absPath()) ;
						continue ;
					}
					WriteDocument wdoc = isession.newDocument(ie.fqn().absPath()).keyword(Defined.Index.PARENT, ie.fqn().getParent().absPath()) ;
					JsonObject jvalue = ie.jsonValue();
					for (String fname : jvalue.keySet()) {
						Property property = Property.create(rsession, ie.fqn(), fname, jvalue.asJsonObject(fname)) ;
						property.indexTo(wdoc) ;
					}
					wdoc.update() ;
				}
				return null;
			}) ;
		}
		
		attrs.clear(); 
	}

	public Workspace workspace() {
		return wspace;
	}

	public WriteSession attribute(String name, Object value) {
		attrs.put(name, value) ;
		return this ;
	}

}
