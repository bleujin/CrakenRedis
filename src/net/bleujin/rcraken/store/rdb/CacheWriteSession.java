package net.bleujin.rcraken.store.rdb;

import java.util.Set;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteNode;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.store.cache.CacheMap;
import net.ion.framework.parse.gson.JsonObject;

public class CacheWriteSession extends WriteSession {

	private CacheMap<Fqn, JsonObject> cacheNode;
	private PGWriteSession iwsession ;
	private CacheReadSession irsession;
	
	protected CacheWriteSession(CacheMap<Fqn, JsonObject> cacheNode, CacheWorkspace workspace, PGWriteSession wsession, ReadSession rsession) {
		super(workspace, rsession) ;
		this.cacheNode = cacheNode ;
		this.iwsession = wsession ;
		this.irsession = (CacheReadSession) rsession ;
	}

	@Override
	protected void merge(WriteNode wnode, Fqn fqn, JsonObject data) {
		iwsession.merge(wnode, fqn, data);
		cacheNode.put(fqn, data) ;
	}

	@Override
	protected void removeChild(WriteNode wnode, Fqn fqn, JsonObject data) {
		iwsession.removeChild(wnode, fqn, data);
		cacheNode.keySet().stream().filter(current -> current.isChildOf(fqn)).forEach(current -> cacheNode.remove(current));
	}

	@Override
	protected void removeSelf(WriteNode wnode, Fqn fqn, JsonObject data) {
		iwsession.removeSelf(wnode, fqn, data);
		cacheNode.remove(fqn) ;
	}

	@Override
	protected Set<String> readStruBy(Fqn fqn) {
		return iwsession.readStruBy(fqn);
	}

	@Override
	protected JsonObject readDataBy(Fqn fqn) {
		return irsession.readDataBy(fqn);
	}

	@Override
	public boolean exist(String path) {
		return irsession.exist(path);
	}

}
