package net.bleujin.rcraken;

import java.util.Map;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import net.ion.framework.util.MapUtil;

public class WriteSession {

	private Workspace wspace;
	private ReadSession rsession;
	private Map<String, Object> attrs = MapUtil.newMap() ;
	private RMap<String, String> cmap;

	WriteSession(Workspace wspace, ReadSession rsession, RedissonClient rclient) {
		this.wspace = wspace ;
		this.rsession = rsession ;
		this.cmap = rclient.getMap(wspace.name(), wspace.mapOption()) ;
	}

	public ReadSession readSession() {
		return rsession ;
	}
	
	public WriteNode pathBy(String path) {
		Fqn fqn = Fqn.from(path) ;
		
		return new WriteNode(this, fqn, cmap);
	}

	public <T> void attribute(Class<T> clz, T obj) {
		attrs.put(clz.getCanonicalName(), obj);
	}

	public <T> T attribute(Class<T> clz) {
		return (T) attrs.get(clz.getCanonicalName());
	}

	public void endBatch() {
		
	}

	public Workspace workspace() {
		return wspace;
	}
}
