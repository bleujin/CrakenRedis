package net.bleujin.rcraken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.InvalidationPolicy;
import org.redisson.api.RBatch;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RListMultimapCache;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;

import net.bleujin.rcraken.def.Defined;
import net.bleujin.rcraken.extend.IndexEvent;
import net.bleujin.rcraken.extend.NodeListener;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.extend.Topic;
import net.bleujin.rcraken.template.TemplateFac;
import net.ion.framework.mte.Engine;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectId;
import net.ion.framework.util.WithinThreadExecutor;
import net.ion.nsearcher.common.WriteDocument;
import net.ion.nsearcher.config.Central;
import net.ion.nsearcher.index.Indexer;

public class Workspace {

	private String wname ;
	private RedissonClient rclient ;
	private LocalCachedMapOptions<String, String> mapOption ;
	private Map<String, NodeListener> listeners = MapUtil.newMap() ;
	private AtomicBoolean inited = new AtomicBoolean(false) ;
	private ExecutorService es = new WithinThreadExecutor() ;
	private RReadWriteLock rwlock ;
	private Central central ;
	private RMapCache<Object, Object> dataMap;
	private Engine parseEngine ;
	private TemplateFac templateFac;

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
		this.dataMap = rclient.getMapCache(nodeMapName());
		this.parseEngine = Engine.createDefaultEngine();
		this.templateFac = new TemplateFac() ;
	}

	public Workspace init() {
		if (!inited.getAndSet(true)) {
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

		for (NodeListener nodeListener : listeners.values()) {
			nodeListener.onChanged(etype, fqn, value, oldValue);
		}
	};

	public Fqn fqnBy(String path) {
		return Fqn.from(path);
	}

	public String name() {
		return wname;
	}

	ExecutorService executor() {
		return es ;
	}
	
	public Engine parseEngine() {
		return parseEngine;
	}
	
	public TemplateFac templateFac() {
		return templateFac ; 
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
	
	public LocalCachedMapOptions<String, String> mapOption() {
		return mapOption;
	}

	WriteSession writeSession(ReadSession rsession) {
		return new WriteSession(this, rsession, rclient);
	}
	
	BatchSession batchSession(ReadSession rsession) {
		return new BatchSession(this, rsession, rclient);
	}

	ReadSession readSession() {
		return new ReadSession(this, rclient);
	}

	<T> CompletableFuture<T> tran(WriteSession wsession, WriteJob<T> tjob, ExecutorService eservice, ExceptionHandler ehandler) {
		return CompletableFuture.supplyAsync(() -> {
			wsession.attribute(WriteJob.class, tjob);
			wsession.attribute(ExceptionHandler.class, ehandler);

			RLock wlock = rwlock.writeLock();
			try {
				wlock.tryLock(10, TimeUnit.MINUTES); // 
				T result = tjob.handle(wsession);
				
				Workspace.this.dataMap.put("__endtran_" + new ObjectId().toString(), "{}", 3, TimeUnit.SECONDS);
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



	
	<T> CompletableFuture<T> batch(BatchSession bsession, BatchJob<T> bjob, ExecutorService eservice, ExceptionHandler ehandler) {
		return CompletableFuture.supplyAsync(() -> {
			bsession.attribute(BatchJob.class, bjob);
			bsession.attribute(ExceptionHandler.class, ehandler);

			RLock wlock = rwlock.writeLock();
			try {
				wlock.tryLock(10, TimeUnit.MINUTES); //
				T result = bjob.handle(bsession);
				RBatch batch = bsession.batch() ;
//				batch.skipResult();// Synchronize write operations execution across defined amount of Redis slave nodes 2 slaves and 1 second timeout
//				batch.syncSlaves(2, 1, TimeUnit.SECONDS) ;
//				batch.timeout(2, TimeUnit.SECONDS); // Response timeout
				batch.retryInterval(2, TimeUnit.SECONDS); // Retry interval for each attempt to send Redis commands batch
				batch.retryAttempts(4); // Attempts amount to re-send Redis commands batch if it hasn't been sent already
				batch.execute();
				
				Workspace.this.dataMap.put("__endtran_" + new ObjectId().toString(), "{}", 3, TimeUnit.SECONDS);
				
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



	public void addListener(NodeListener nodeListener) {
		listeners.put(nodeListener.id(), nodeListener);
	}

	public Sequence sequence(String name) {
		return new Sequence(name, rclient.getAtomicLong(seqPrefix() + name));
	}

	public <T> Topic<T> topic(String name) {
		return new Topic<T>(name, rclient.getTopic(topicPrefix() + name));
	}

	
	public boolean removeSelf() {
		rclient.getKeys().deleteByPattern(name() + ".*") ;
		rclient.getKeys().deleteByPattern("{" + name() + ".*") ; // ?? 
		
		
//		rclient.getMap(nodeMapName()).delete();
//		rclient.getSetMultimap(struMapName()).delete();
		if (central != null) central.destroySelf(); 
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

	
	private List<IndexEvent> ievents = ListUtil.newList() ;
	private boolean hasIndexer() {
		return listeners.containsKey(indexListenerId()) && central != null;
	}
	String indexListenerId() {
		return name() + ".indexer" ;
	}
	
	
	public Central central() {
		return central ;
	}
	
	
	public Workspace indexCntral(Central central) {
		this.central = central ;
		this.addListener(new NodeListener() {
			public void onChanged(EventType etype, Fqn fqn, JsonObject jvalue, JsonObject oldValue) {
				if (fqn.absPath().startsWith("/__endtran")) {
					List<IndexEvent> ies = Workspace.this.ievents ;
					Workspace.this.ievents = ListUtil.newList() ;
					
					Indexer indexer = central.newIndexer() ;
					indexer.index(isession -> {
						for (IndexEvent ie : ies) {
							if (ie.eventType() == EventType.REMOVED) {
								isession.deleteById(ie.fqn().absPath()) ;
								continue ;
							}
							WriteDocument wdoc = isession.newDocument(ie.fqn().absPath()).keyword(Defined.Index.PARENT, ie.fqn().getParent().absPath()) ;
							JsonObject newvalue = ie.jsonValue();
							for (String fname : newvalue.keySet()) {
								Property property = Property.create(null, ie.fqn(), fname, newvalue.asJsonObject(fname)) ;
								property.indexTo(wdoc) ;
							}
							wdoc.update() ;
						}
						return null;
					}) ;
					return ;
				}
				if (! jvalue.keySet().isEmpty() ) {
					Workspace.this.ievents.add(IndexEvent.create(etype, fqn, jvalue)) ;
				}
			}

			@Override
			public String id() {
				return indexListenerId();
			}
		});
		
		return this ;
	}
	
	public Workspace reindex(boolean clearOld) {
		if (! hasIndexer()) throw new IllegalStateException("central not exists") ;
		Indexer indexer = central.newIndexer() ;
		indexer.index(isession -> {
			if(clearOld) isession.deleteAll() ;
			writeSession(readSession()).pathBy("/").walkBreadth().forEach(node -> {
				try {
					WriteDocument wdoc = isession.newDocument(node.fqn().absPath()).keyword(Defined.Index.PARENT, node.fqn().getParent().absPath()) ;
					node.properties().iterator().forEachRemaining(property -> property.indexTo(wdoc));
					wdoc.update() ;
				} catch(IOException e) {
					throw new IllegalStateException(e) ;
				}
			});
			
			return null ;
		}) ;
		return this ;
	}
	

	public void removeListener(String id) {
		listeners.remove(id) ;
	}

}
