package net.bleujin.rcraken;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.InvalidationPolicy;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import net.ion.framework.util.WithinThreadExecutor;

public class Workspace {

	private String wname;
	private RedissonClient rclient;
	private LocalCachedMapOptions<String, String> mapOption;

	Workspace(String wname, RedissonClient rclient) {
		this.wname = wname ;
		this.rclient = rclient ;
		this.mapOption = LocalCachedMapOptions.<String, String>defaults()
			      // LFU, LRU, SOFT, WEAK and NONE policies are available
			     .evictionPolicy(EvictionPolicy.LRU)
			      // If cache size is 0 then local cache is unbounded.
			     .cacheSize(1000)
			      // if value is `ON_CHANGE`, `ON_CHANGE_WITH_CLEAR_ON_RECONNECT` or `ON_CHANGE_WITH_LOAD_ON_RECONNECT` 
			      // corresponding map entry is removed from cache across all RLocalCachedMap instances 
			      // during every invalidation message sent on each map entry update/remove operation
			     .invalidationPolicy(InvalidationPolicy.ON_CHANGE)
			      // time to live for each map entry in local cache
			     .timeToLive(10, TimeUnit.SECONDS)
			      // max idle time for each map entry in local cache
			     .maxIdle(10, TimeUnit.SECONDS);
	}

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

	
	private ExecutorService es = new WithinThreadExecutor() ;
	<T> Future<T> tran(WriteSession wsession, TransactionJob<T> tjob, ExceptionHandler ehandler) {
		return es.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				
				
				wsession.attribute(TransactionJob.class, tjob);
				wsession.attribute(ExceptionHandler.class, ehandler);
//				wsession.attribute(CDDMListener.class, cddm());

				RLock wlock = rclient.getReadWriteLock(name()).writeLock() ;
				try {
					wlock.lock(10, TimeUnit.SECONDS); // wait during 10 sec
					
					T result = tjob.handle(wsession) ;
					wsession.endBatch() ;
					
					wsession.readSession().reload(); 
					return result ;
				} catch(Exception ex) {
					if (ehandler == null)
						throw new IllegalStateException(ex);
					ehandler.handle(wsession, tjob, ex);
					return null ;
				} finally {
					wlock.unlock(); 
				}
			}
		}) ;
		
	}

	
	public boolean flushAll() {
		rclient.getMap(wname).delete();
		rclient.getSetMultimap(struMapName()).delete();
		
		return true ;
	}

	
}
