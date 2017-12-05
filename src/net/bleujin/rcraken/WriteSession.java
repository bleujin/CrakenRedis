package net.bleujin.rcraken;

import java.util.Map;

import org.redisson.api.RListMultimap;
import org.redisson.api.RMap;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetMultimapCache;
import org.redisson.api.RedissonClient;

import net.ion.framework.util.MapUtil;

public class WriteSession {

	private Workspace wspace;
	private ReadSession rsession;
	private Map<String, Object> attrs = MapUtil.newMap() ;
	private RMap<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	WriteSession(Workspace wspace, ReadSession rsession, RedissonClient rclient) {
		this.wspace = wspace ;
		this.rsession = rsession ;
		this.dataMap = rclient.getMap(wspace.name()) ;
		this.struMap = rclient.getSetMultimap(wspace.struMapName()) ;
	}

	public ReadSession readSession() {
		return rsession ;
	}
	
	public WriteNode pathBy(String path) {
		Fqn fqn = Fqn.from(path) ;
		Fqn current = fqn ;
		
		while(! current.isRoot()) {
			if (! dataMap.containsKey(current.absPath())) {
				struMap.put(current.getParent().absPath(), current.name()) ;
				dataMap.put(current.absPath(), "{}") ;
			}
			if (current.isRoot()) break ;
			current = current.getParent() ;
		}
		
		return new WriteNode(this, fqn, dataMap);
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
