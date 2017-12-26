package net.bleujin.rcraken;

import java.util.Map;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.MapUtil;

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
