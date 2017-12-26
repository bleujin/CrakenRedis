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

public abstract class BatchSession {

	private Workspace wspace;
	private ReadSession rsession;
	private Map<String, Object> attrs = MapUtil.newMap();
	public BatchSession(Workspace wspace, ReadSession rsession) {
		this.wspace = wspace ;
		this.rsession = rsession ;
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

	protected abstract void insert(BatchNode wnode, Fqn fqn, JsonObject data) ;
	
	
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
