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

import org.redisson.api.RedissonClient;

import net.bleujin.rcraken.def.Defined;
import net.bleujin.rcraken.extend.IndexEvent;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.SetUtil;
import net.ion.nsearcher.common.WriteDocument;
import net.ion.nsearcher.index.Indexer;

public class BatchSession {

	private Workspace wspace;
	private ReadSession rsession;
	private Map<String, Object> attrs = MapUtil.newMap();
	private RedissonClient rclient;

	public BatchSession(Workspace wspace, ReadSession rsession, RedissonClient rclient) {
		this.wspace = wspace ;
		this.rsession = rsession ;
		this.rclient = rclient ;
	}


	public ReadSession readSession() {
		return rsession;
	}

	public BatchNode pathBy(String path) {
		Fqn fqn = Fqn.from(path);

		return pathBy(fqn);
	}

	public BatchNode pathBy(Fqn fqn) {
		return new BatchNode(this, fqn, new JsonObject());
	}

	void insert(BatchNode wnode, Fqn fqn, JsonObject data) {
//		Fqn current = fqn;
//		while (!current.isRoot()) {
//			if (!dataMap.containsKey(current.absPath())) {
//				struMap.put(current.getParent().absPath(), current.name());
//				dataMap.put(current.absPath(), "{}");
//			}
//			current = current.getParent();
//		}
//		dataMap.put(fqn.absPath(), data.toString());
//		
//		// handle lob
//		childJson(data).filter(p -> "Lob".equals(p.asString("type"))&& hasAttribute(p.asString("value"))).forEach(p -> {
//			if (attrs.get(p.asString("value")) instanceof InputStream) {
//		        try {
//		        	InputStream input = (InputStream) attrs.get(p.asString("value")) ;
//		        	OutputStream output = wspace.outputStream(p.asString("value"));
//					IOUtil.copyNClose(input, output);
//				} catch (IOException ex) {
//					attrs.put(p.asString("value"), ex.getMessage()) ;
//				}
//			}
//		});
		
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

	public BatchSession attribute(String name, Object value) {
		attrs.put(name, value) ;
		return this ;
	}

}
