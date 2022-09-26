package net.bleujin.rcraken.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.InvalidationPolicy;
import org.redisson.api.RBatch;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;

import net.bleujin.rcraken.BatchJob;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.ExceptionHandler;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.bleujin.rcraken.extend.RedisSequence;
import net.bleujin.rcraken.extend.Topic;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.ObjectId;

public class RedisWorkspace extends Workspace{

	private RedissonClient rclient ;
	private LocalCachedMapOptions<String, String> mapOption ;
	private AtomicBoolean inited = new AtomicBoolean(false) ;
	private RReadWriteLock rwlock ;
	private RMapCache<Object, Object> dataMap;

	protected RedisWorkspace(CrakenNode cnode, String wname, RedissonClient rclient) {
		super(cnode, wname) ;
		this.rclient = rclient;
		this.mapOption = LocalCachedMapOptions.<String, String>defaults().evictionPolicy(EvictionPolicy.LRU) // LFU, LRU, SOFT, WEAK and NONE policies are available
				.cacheSize(1000) // If cache size is 0 then local cache is unbounded.
				// if value is `ON_CHANGE`, `ON_CHANGE_WITH_CLEAR_ON_RECONNECT` or `ON_CHANGE_WITH_LOAD_ON_RECONNECT`
				// corresponding map entry is removed from cache across all RLocalCachedMap
				// instances during every invalidation message sent on each map entry update/remove operation
				.invalidationPolicy(InvalidationPolicy.ON_CHANGE).timeToLive(10, TimeUnit.SECONDS) // time to live for each map entry in local cache
				.maxIdle(10, TimeUnit.SECONDS); // max idle time for each map entry in local cache
		
		this.rwlock = rclient.getReadWriteLock(wname + ".rwlock");
		this.dataMap = rclient.getMapCache(nodeMapName());
	}

	public Workspace init() {
		if (!inited.getAndSet(true)) {
			dataMap.addListener(new EntryUpdatedListener<String, String>() {
				@Override
				public void onUpdated(EntryEvent<String, String> event) {
					RedisWorkspace.this.onMerged(EventType.UPDATED, event.getKey(), event.getValue(), event.getOldValue());
				}
			});
			dataMap.addListener(new EntryCreatedListener<String, String>() {
				@Override
				public void onCreated(EntryEvent<String, String> event) {
					RedisWorkspace.this.onMerged(EventType.CREATED, event.getKey(), event.getValue(), event.getOldValue());
				}
			});
			dataMap.addListener(new EntryRemovedListener<String, String>() {
				@Override
				public void onRemoved(EntryEvent<String, String> event) {
					RedisWorkspace.this.onMerged(EventType.REMOVED, event.getKey(), event.getValue(), event.getOldValue());
				}
			});
		}
		return this;
	}

	private JsonObject toJson(String jsonValue) {
		return jsonValue == null ? null : JsonObject.fromString(jsonValue);
	}


	
	String nodeMapName(){
		return name() + ".node" ;
	}
	
	String struMapName() {
		return name() + ".stru";
	}

	String lobPrefix(){
		return name() + ".lob" ;
	}

	String seqPrefix(){
		return name() + ".seq." ;
	}
	
	String topicPrefix(){
		return name() + ".topic." ;
	}
	
	public LocalCachedMapOptions<String, String> mapOption() {
		return mapOption;
	}

	protected WriteSession writeSession(ReadSession rsession) {
		return new RedisWriteSession(this, rsession, rclient);
	}
	
	protected BatchSession batchSession(ReadSession rsession) {
		return new RedisBatchSession(this, rsession, rclient);
	}

	protected ReadSession readSession() {
		return new RedisReadSession(this, rclient);
	}

	protected <T> CompletableFuture<T> tran(WriteSession wsession, WriteJob<T> tjob, ExecutorService eservice, ExceptionHandler ehandler) {
		if (eservice.isTerminated() || eservice.isShutdown()) return CompletableFuture.completedFuture(null) ;
		
		return CompletableFuture.supplyAsync(() -> {
			wsession.attribute(WriteJob.class, tjob);
			wsession.attribute(ExceptionHandler.class, ehandler);

			RLock wlock = rwlock.writeLock();
			try {
				wlock.tryLock(10, TimeUnit.MINUTES); // 
				T result = tjob.handle(wsession);
				
				RedisWorkspace.this.dataMap.put("__endtran_" + new ObjectId().toString(), "{}", 3, TimeUnit.SECONDS);
				wsession.endTran();
				return result;
			} catch (Throwable ex) {
				ehandler.handle(wsession, tjob, ex);
				throw new IllegalStateException(ex) ; 
			} finally {
				wlock.unlock(); 
			}
		}, eservice) ;
	}



	
	protected <T> CompletableFuture<T> batch(BatchSession bsession, BatchJob<T> bjob, ExecutorService eservice, ExceptionHandler ehandler) {
		if (eservice.isTerminated() || eservice.isShutdown()) return CompletableFuture.completedFuture(null) ;
		
		return CompletableFuture.supplyAsync(() -> {
			bsession.attribute(BatchJob.class, bjob);
			bsession.attribute(ExceptionHandler.class, ehandler);

			RLock wlock = rwlock.writeLock();
			try {
				wlock.tryLock(10, TimeUnit.MINUTES); //
				T result = bjob.handle(bsession);
				RBatch batch = ((RedisBatchSession)bsession).batch() ;
//				batch.skipResult();// Synchronize write operations execution across defined amount of Redis slave nodes 2 slaves and 1 second timeout
//				batch.syncSlaves(2, 1, TimeUnit.SECONDS) ;
//				batch.timeout(2, TimeUnit.SECONDS); // Response timeout
				batch.retryInterval(2, TimeUnit.SECONDS); // Retry interval for each attempt to send Redis commands batch
				batch.retryAttempts(4); // Attempts amount to re-send Redis commands batch if it hasn't been sent already
				batch.execute();
				
				RedisWorkspace.this.dataMap.put("__endtran_" + new ObjectId().toString(), "{}", 3, TimeUnit.SECONDS);
				
				bsession.endTran();
				return result ;
			} catch (Throwable ex) {
				ehandler.handle(bsession, bjob, ex);
				throw new IllegalStateException(ex) ; 
			} finally {
				wlock.unlock(); 
			}
		}, eservice) ;
	}


	public RedisSequence sequence(String name) {
		return new RedisSequence(name, rclient.getAtomicLong(seqPrefix() + name));
	}

	public <T> Topic<T> topic(String name) {
		return new Topic<T>(name, rclient.getTopic(topicPrefix() + name));
	}

	
	public boolean removeSelf() throws IOException {
		super.removeSelf() ;
		
		rclient.getKeys().deleteByPattern(name() + ".*") ;
		rclient.getKeys().deleteByPattern("{" + name() + ".*") ; // ?? 
		
		return true;
	}

	protected OutputStream outputStream(String path) {
		RBinaryStream binaryStream = rclient.getBinaryStream(lobPrefix() + path);
		if (binaryStream.isExists()) {
			binaryStream.delete();
		}
		return binaryStream.getOutputStream();
	}

	protected InputStream inputStream(String path) {
		return rclient.getBinaryStream(lobPrefix() + path).getInputStream();
	}

	@Deprecated //test only
	RedissonClient client() {
		return rclient;
	}

}
