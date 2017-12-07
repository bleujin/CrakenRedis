package net.bleujin.rcraken;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.InvalidationPolicy;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import net.bleujin.rcraken.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.WithinThreadExecutor;

public class Workspace {

	private String wname;
	private RedissonClient rclient;
	private LocalCachedMapOptions<String, String> mapOption;
	private List<NodeListener> listeners = ListUtil.newList() ;
	private AtomicBoolean inited = new AtomicBoolean(false) ;
	private ExecutorService es = new WithinThreadExecutor() ;
	
	Workspace(String wname, RedissonClient rclient) {
		this.wname = wname ;
		this.rclient = rclient ;
		this.mapOption = LocalCachedMapOptions.<String, String>defaults()
			     .evictionPolicy(EvictionPolicy.LRU) // LFU, LRU, SOFT, WEAK and NONE policies are available
			     .cacheSize(1000) // If cache size is 0 then local cache is unbounded.
			      // if value is `ON_CHANGE`, `ON_CHANGE_WITH_CLEAR_ON_RECONNECT` or `ON_CHANGE_WITH_LOAD_ON_RECONNECT` 
			      // corresponding map entry is removed from cache across all RLocalCachedMap instances 
			      // during every invalidation message sent on each map entry update/remove operation
			     .invalidationPolicy(InvalidationPolicy.ON_CHANGE)
			     .timeToLive(10, TimeUnit.SECONDS) // time to live for each map entry in local cache
			     .maxIdle(10, TimeUnit.SECONDS); // max idle time for each map entry in local cache
	}

	public Workspace init() {
		if (!inited.getAndSet(true)) {
//			rclient.getMapCache(name()).addListener(new EntryUpdatedListener<String, String>() {
//				@Override
//				public void onUpdated(EntryEvent<String, String> event) {
//					Workspace.this.onMerged(EventType.UPDATED, event.getKey(), event.getValue(), event.getOldValue()) ;
//				}
//			}) ;
//			rclient.getMapCache(name()).addListener(new EntryCreatedListener<String, String>() {
//				@Override
//				public void onCreated(EntryEvent<String, String> event) {
//					Workspace.this.onMerged(EventType.CREATED, event.getKey(), event.getValue(), event.getOldValue()) ;
//				}
//			}) ;
//			rclient.getMapCache(name()).addListener(new EntryRemovedListener<String, String>() {
//				@Override
//				public void onRemoved(EntryEvent<String, String> event) {
//					Workspace.this.onMerged(EventType.REMOVED, event.getKey(), event.getValue(), event.getOldValue()) ;
//				}
//			}) ;
			
		}
		return this;
	}

	private JsonObject toJson(String jsonValue) {
		return jsonValue == null ? null : JsonObject.fromString(jsonValue);
	}
	
	void onMerged(EventType etype, String _fqn, String _value, String _oldValue) {
		Fqn fqn = Fqn.from(_fqn) ;
		JsonObject value = toJson(_value) ;
		JsonObject oldValue = toJson(_oldValue) ;
		
		for (NodeListener nodeListener : listeners) {
			nodeListener.onMerged(etype, fqn, value, oldValue);
		}
	};
	
	
	public Fqn fqnBy(String path) {
		return Fqn.from(path) ;
	}

	public String name() {
		return wname;
	}
	
	String struMapName() {
		return "_" + wname;
	}
	
	public LocalCachedMapOptions<String, String> mapOption(){
		return mapOption ;
	}


	WriteSession writeSession(ReadSession rsession) {
		return new WriteSession(this, rsession, rclient);
	}

	ReadSession readSession() {
		return new ReadSession(this, rclient);
	}

	
	<T> Future<T> tran(WriteSession wsession, TransactionJob<T> tjob, ExceptionHandler ehandler) {
		return es.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				
				wsession.attribute(TransactionJob.class, tjob);
				wsession.attribute(ExceptionHandler.class, ehandler);

				RLock wlock = rclient.getReadWriteLock(name()).writeLock() ;
				try {
					wlock.tryLock(1000, TimeUnit.MILLISECONDS); // wait during 1000 ms
					
					T result = tjob.handle(wsession) ;
					wsession.endBatch() ;
					wsession.readSession().reload(); 
					return result ;
				} catch(Throwable ex) {
					ehandler.handle(wsession, tjob, ex);
					return null ;
				} finally {
//					wlock.unlink();
					wlock.unlock(); // @FixMe : Why??? unlock not working. 
				}
			}
		}) ;
		
	}

	public void addListener(NodeListener nodeListener) {
		listeners.add(nodeListener) ;
	}


	public boolean destorySelf() {
		rclient.getMap(wname).delete();
		rclient.getSetMultimap(struMapName()).delete();
		listeners.clear();
		
		return true ;
	}

	
}
