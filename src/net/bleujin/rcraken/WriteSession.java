package net.bleujin.rcraken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.redisson.api.RMap;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.SetUtil;

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
		this.dataMap = rclient.getMapCache(wspace.nodeMapName());
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
	
	public void copySelf(WriteNode sourceNode, Fqn sourceFqn, JsonObject sourceData, String destPath) {
		Fqn dest = Fqn.from(destPath) ;
		sourceNode.children().forEach(wn -> {
			// if (exist(destPath)) throw new IllegalStateException("destPath already exist :" + destPath) ;
			wn.copySelf(Fqn.from(dest, wn.fqn().name()).absPath());
		});
		merge(pathBy(destPath), dest, sourceData) ;
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
	
	public boolean hasAttribute(String name) {
		return attrs.containsKey(name) ;
	}

	public <T> void attribute(Class<T> clz, T obj) {
		attrs.put(clz.getCanonicalName(), obj);
	}

	public <T> T attribute(Class<T> clz) {
		return (T) attrs.get(clz.getCanonicalName());
	}

	public void endTran() {
		attrs.clear(); 
	}

	public Workspace workspace() {
		return wspace;
	}

	public WriteSession attribute(String name, Object value) {
		attrs.put(name, value) ;
		return this ;
	}

	Set<String> readStruBy(Fqn fqn) {
		return struMap.getAll(fqn.absPath());
	}
	
	void descentantBreadth(Fqn fqn, List<String> fqns) {
		for(String childName : readStruBy(fqn)) {
			Fqn child = Fqn.from(fqn, childName);
			fqns.add(child.absPath()) ;
			descentantBreadth(child, fqns);
		}
	}

	void descentantDepth(Fqn fqn, List<String> fqns) {
		for(String childName : readStruBy(fqn)) {
			Fqn child = Fqn.from(fqn, childName);
			fqns.add(child.absPath()) ;
		}

		for(String childName : readStruBy(fqn)) {
			descentantDepth(Fqn.from(fqn, childName), fqns);
		}
	}

	public boolean exist(String path) {
		Fqn fqn = Fqn.from(path);
		return fqn.isRoot() || dataMap.containsKey(fqn.absPath());
	}

	public void walkRef(WriteNode source, String relName, int limit, List<String> fqns) {
		if (limit == 0) return ; 
		for(String relPath : source.property(relName).asStrings()) {
			Fqn rel = Fqn.from(relPath);
			if (! source.session().exist(rel.absPath())) continue ;
			fqns.add(rel.absPath()) ;
			walkRef(source.session().pathBy(rel), relName, --limit, fqns);
		}
	}

}
