package net.bleujin.rcraken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.ecs.xhtml.map;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RMapAsync;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetMultimapCache;
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
	private RBatch batch;
	private RMapAsync<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	public BatchSession(Workspace wspace, ReadSession rsession, RedissonClient rclient) {
		this.wspace = wspace ;
		this.rsession = rsession ;
		this.rclient = rclient ;
		this.batch = rclient.createBatch();
		this.dataMap = batch.getMapCache(wspace.nodeMapName());
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName());
	}


	public ReadSession readSession() {
		return rsession;
	}

	RBatch batch() {
		return batch ;
	}
	
	public BatchNode pathBy(String path) {
		Fqn fqn = Fqn.from(path);

		return pathBy(fqn);
	}

	public BatchNode pathBy(Fqn fqn) {
		return new BatchNode(this, fqn, new JsonObject());
	}

	void insert(BatchNode wnode, Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {
			
			if (! (struMap.containsKey(current.absPath()) && struMap.get(current.getParent().absPath()).contains(current.absPath())  )) {
				struMap.put(current.getParent().absPath(), current.name());
				dataMap.putAsync(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		dataMap.fastPutAsync(fqn.absPath(), data.toString());
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

	public BatchSession attribute(String name, Object value) {
		attrs.put(name, value) ;
		return this ;
	}

}
