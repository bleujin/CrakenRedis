package net.bleujin.rcraken;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.InvalidationPolicy;
import org.redisson.api.RBinaryStream;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import net.bleujin.rcraken.extend.NodeListener;
import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.extend.Topic;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.SetUtil;
import net.ion.framework.util.WithinThreadExecutor;

public class Workspace {

	private String wname;
	private RedissonClient rclient;
	private LocalCachedMapOptions<String, String> mapOption;
	private List<NodeListener> listeners = ListUtil.newList();
	private AtomicBoolean inited = new AtomicBoolean(false);
	private ExecutorService es = new WithinThreadExecutor();
	private RReadWriteLock rwlock;

	Workspace(String wname, RedissonClient rclient) {
		this.wname = wname;
		this.rclient = rclient;
		this.mapOption = LocalCachedMapOptions.<String, String>defaults().evictionPolicy(EvictionPolicy.LRU) // LFU, LRU, SOFT, WEAK and NONE policies are available
				.cacheSize(1000) // If cache size is 0 then local cache is unbounded.
				// if value is `ON_CHANGE`, `ON_CHANGE_WITH_CLEAR_ON_RECONNECT` or `ON_CHANGE_WITH_LOAD_ON_RECONNECT`
				// corresponding map entry is removed from cache across all RLocalCachedMap
				// instances during every invalidation message sent on each map entry update/remove operation
				.invalidationPolicy(InvalidationPolicy.ON_CHANGE).timeToLive(10, TimeUnit.SECONDS) // time to live for each map entry in local cache
				.maxIdle(10, TimeUnit.SECONDS); // max idle time for each map entry in local cache
		
		this.rwlock = rclient.getReadWriteLock(wname + ".rwlock");
	}

	public Workspace init() {
		if (!inited.getAndSet(true)) {
			RMapCache<String, String> dataMap = rclient.getMapCache(nodeMapName());
			dataMap.addListener(new EntryUpdatedListener<String, String>() {
				@Override
				public void onUpdated(EntryEvent<String, String> event) {
					Workspace.this.onMerged(EventType.UPDATED, event.getKey(), event.getValue(), event.getOldValue());
				}
			});
			dataMap.addListener(new EntryCreatedListener<String, String>() {
				@Override
				public void onCreated(EntryEvent<String, String> event) {
					Workspace.this.onMerged(EventType.CREATED, event.getKey(), event.getValue(), event.getOldValue());
				}
			});
			dataMap.addListener(new EntryRemovedListener<String, String>() {
				@Override
				public void onRemoved(EntryEvent<String, String> event) {
					Workspace.this.onMerged(EventType.REMOVED, event.getKey(), event.getValue(), event.getOldValue());
				}
			});
		}
		return this;
	}

	private JsonObject toJson(String jsonValue) {
		return jsonValue == null ? null : JsonObject.fromString(jsonValue);
	}

	void onMerged(EventType etype, String _fqn, String _value, String _oldValue) {
		Fqn fqn = Fqn.from(_fqn);
		JsonObject value = toJson(_value);
		JsonObject oldValue = toJson(_oldValue);

		for (NodeListener nodeListener : listeners) {
			nodeListener.onMerged(etype, fqn, value, oldValue);
		}
	};

	public Fqn fqnBy(String path) {
		return Fqn.from(path);
	}

	public String name() {
		return wname;
	}

	public Workspace executor(ExecutorService es) {
		this.es = es ;
		return this ;
	}
	
	String nodeMapName(){
		return wname + ".node" ;
	}
	
	String struMapName() {
		return wname + ".stru";
	}

	String lobPrefix(){
		return wname + ".lob" ;
	}

	String seqPrefix(){
		return wname + ".seq." ;
	}
	
	String topicPrefix(){
		return wname + ".topic." ;
	}
	
	LocalCachedMapOptions<String, String> mapOption() {
		return mapOption;
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

				RLock wlock = rwlock.writeLock();
				// WriteLock wlock = rwlock.writeLock() ;
				try {
					wlock.tryLock(10, TimeUnit.MINUTES); // 

					T result = tjob.handle(wsession);
					wsession.endBatch();
					wsession.readSession().reload();
					return result;
				} catch (Throwable ex) {
					ehandler.handle(wsession, tjob, ex);
					return null;
				} finally {
					// wlock.unlink();
					wlock.unlock(); // @FixMe : Why??? redisson unlock not working.
				}
			}
		});

	}

	public void addListener(NodeListener nodeListener) {
		listeners.add(nodeListener);
	}

	public Sequence sequence(String name) {
		return new Sequence(name, rclient.getAtomicLong(seqPrefix() + name));
	}

	public <T> Topic<T> topic(String name) {
		return new Topic<T>(name, rclient.getTopic(topicPrefix() + name));
	}

	
	public boolean destorySelf() {
		rclient.getKeys().deleteByPattern(name() + ".*") ; // blob
//		rclient.getMap(nodeMapName()).delete();
//		rclient.getSetMultimap(struMapName()).delete();
		listeners.clear();

		return true;
	}

	OutputStream outputStream(String path) {
		RBinaryStream binaryStream = rclient.getBinaryStream(lobPrefix() + path);
		if (binaryStream.isExists()) {
			binaryStream.delete();
		}
		return binaryStream.getOutputStream();
	}

	InputStream inputStream(String path) {
		return rclient.getBinaryStream(lobPrefix() + path).getInputStream();
	}

	@Deprecated //test only
	RedissonClient client() {
		return rclient;
	}




}
