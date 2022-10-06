package net.bleujin.rcraken.store.rdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.apache.commons.collections.map.LRUMap;

import net.bleujin.rcraken.BatchJob;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.ExceptionHandler;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.extend.NodeListener;
import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.extend.Topic;
import net.bleujin.rcraken.store.cache.CacheMap;
import net.ion.framework.parse.gson.JsonObject;

public class CacheWorkspace extends Workspace{

	private PGWorkspace iworkspace;
	private CacheMap<Fqn, JsonObject> cacheNode ;
	
	protected CacheWorkspace(PGWorkspace iworkspace, CrakenNode cnode, String wname, int cacheSize) {
		super(cnode, wname);
		this.iworkspace = iworkspace ;
		cacheNode = new CacheMap<>(cacheSize) ;
	}

	@Override
	protected WriteSession writeSession(ReadSession rsession) {
		return new CacheWriteSession(cacheNode, this, iworkspace.writeSession( ((CacheReadSession)rsession).innerReadSession() ), rsession);
	}

	@Override
	public void addListener(NodeListener nodeListener) {
		iworkspace.addListener(nodeListener);
	}

	
	@Override
	protected BatchSession batchSession(ReadSession rsession) {
		return new CacheBatchSession(cacheNode, this, iworkspace.batchSession(rsession));
	}

	@Override
	protected ReadSession readSession() {
		return new CacheReadSession(cacheNode, this, iworkspace.readSession());
	}

	@Override
	protected <T> CompletableFuture<T> tran(WriteSession wsession, WriteJob<T> tjob, ExecutorService eservice, ExceptionHandler ehandler) {
		return iworkspace.tran(wsession, tjob, eservice, ehandler);
	}

	@Override
	protected <T> CompletableFuture<T> batch(BatchSession bsession, BatchJob<T> bjob, ExecutorService eservice, ExceptionHandler ehandler) {
		return iworkspace.batch(bsession, bjob, eservice, ehandler);
	}

	@Override
	public Sequence sequence(String name) {
		return iworkspace.sequence(name);
	}

	@Override
	public <T> Topic<T> topic(String name) {
		return iworkspace.topic(name);
	}

	@Override
	protected OutputStream outputStream(String path) throws IOException {
		return iworkspace.outputStream(path);
	}

	@Override
	protected InputStream inputStream(String path) throws IOException {
		return iworkspace.inputStream(path);
	}

}
