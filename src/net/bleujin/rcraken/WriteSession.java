package net.bleujin.rcraken;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.redisson.api.RListMultimap;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetMultimapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryUpdatedListener;
import org.redisson.api.map.event.MapEntryListener;

import net.bleujin.rcraken.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;

public class WriteSession {

	private Workspace wspace;
	private ReadSession rsession;
	private Map<String, Object> attrs = MapUtil.newMap() ;
	private RMap<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	WriteSession(Workspace wspace, ReadSession rsession, RedissonClient rclient) {
		this.wspace = wspace ;
		this.rsession = rsession ;
		this.dataMap = rclient.getMapCache(wspace.name(), wspace.mapOption()) ;
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName()) ;
	}

	public ReadSession readSession() {
		return rsession ;
	}
	
	public WriteNode pathBy(String path) {
		Fqn fqn = Fqn.from(path) ;

		return pathBy(fqn);
	}

	public WriteNode pathBy(Fqn fqn) {
		return new WriteNode(this, fqn, ObjectUtil.coalesce( JsonObject.fromString(dataMap.get(fqn.absPath())), new JsonObject()));
	}
	
	
	void merge(Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {
			if (!dataMap.containsKey(current.absPath())) {
				struMap.put(current.getParent().absPath(), current.name());
				dataMap.put(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		dataMap.put(fqn.absPath(), data.toString()) ;
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
