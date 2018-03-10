package net.bleujin.rcraken.store;

import java.util.Set;

import org.redisson.api.RMap;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;

import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.parse.gson.JsonObject;

public class RedisReadSession extends ReadSession {

	private RedissonClient rclient;

	private RMap<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	protected RedisReadSession(RedisWorkspace wspace, RedissonClient rclient) {
		super(wspace);
		this.rclient = rclient;
		this.dataMap = rclient.getMapCache(wspace.nodeMapName());
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName());
	}

	public boolean exist(String path) {
		Fqn fqn = Fqn.from(path);
		return fqn.isRoot() || dataMap.containsKey(fqn.absPath());
	}
	
	protected JsonObject readDataBy(Fqn fqn) {
		String jsonString = dataMap.get(fqn.absPath());
		return JsonObject.fromString(jsonString);
	}

	protected Set<String> readStruBy(Fqn fqn) {
		return struMap.getAll(fqn.absPath());
	}

	@Deprecated
	RMap<String, String> dataMap() {
		return dataMap;
	}

}
