package net.bleujin.rcraken.store.rdb;

import java.util.Set;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.store.cache.CacheMap;
import net.bleujin.rcraken.store.cache.FutureValue;
import net.ion.framework.parse.gson.JsonObject;

public class CacheReadSession extends ReadSession{

	private CacheMap<Fqn, JsonObject> cachedData;
	private PGReadSession ireadsession;
	protected CacheReadSession(CacheMap<Fqn, JsonObject> cacheNode, Workspace wspace, PGReadSession readsession) {
		super(wspace);
		this.cachedData = cacheNode ;
		this.ireadsession = readsession ;
		
	}

	@Override
	public boolean exist(String path) {
		return cachedData.containsKey(Fqn.from(path)) || ireadsession.exist(path);
	}

	@Override
	protected JsonObject readDataBy(Fqn fqn) {
		return cachedData.get(fqn, () -> ireadsession.readDataBy(fqn)) ;
	}

	@Override
	protected Set<String> readStruBy(Fqn fqn) {
		return ireadsession.readStruBy(fqn);
	}
	
	PGReadSession innerReadSession() {
		return ireadsession ;
	}

}
