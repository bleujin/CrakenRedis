package net.bleujin.rcraken.store.rdb;

import net.bleujin.rcraken.BatchNode;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.store.cache.CacheMap;
import net.ion.framework.parse.gson.JsonObject;

public class CacheBatchSession extends BatchSession {

	private CacheMap<Fqn, JsonObject> cacheNode;
	private PGBatchSession bsession;

	protected CacheBatchSession(CacheMap<Fqn, JsonObject> cacheNode, CacheWorkspace workspace, PGBatchSession bsession) {
		super(workspace, bsession.readSession()) ;
		this.cacheNode = cacheNode ;
		this.bsession = bsession ;
	}

	@Override
	protected void insert(BatchNode wnode, Fqn fqn, JsonObject data) {
		bsession.insert(wnode, fqn, data);
		cacheNode.put(fqn, data) ;
	}

}
