package net.bleujin.rcraken.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.MapModificationListener;
import org.mapdb.Serializer;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RBatch;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.InvalidationPolicy;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;

import net.bleujin.rcraken.BatchJob;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.ExceptionHandler;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.Property;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.def.Defined;
import net.bleujin.rcraken.extend.IndexEvent;
import net.bleujin.rcraken.extend.NodeListener;
import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.extend.Topic;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.bleujin.rcraken.template.TemplateFac;
import net.ion.framework.mte.Engine;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectId;
import net.ion.framework.util.WithinThreadExecutor;
import net.ion.nsearcher.common.WriteDocument;
import net.ion.nsearcher.config.Central;
import net.ion.nsearcher.index.Indexer;

public class MapWorkspace extends Workspace{

	
	private Map<String, NodeListener> listeners = MapUtil.newMap() ;
	private AtomicBoolean inited = new AtomicBoolean(false) ;
	private Central central ;
	private HTreeMap<String, String> dataMap;
	private DB db;
	private ReadWriteLock rwlock;
	
	public MapWorkspace(String wname, MapNode mnode, DB db) {
		super(wname) ;
		this.db = db;
		this.rwlock = mnode.rwLock(wname + ".rwlock");
	}

	public Workspace init() {
		if (!inited.getAndSet(true)) {
			this.dataMap = db.hashMap(nodeMapName()).keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING)
					.modificationListener(new MapModificationListener<String, String>() {
						@Override
						public void modify(String key, String oldValue, String newValue, boolean triggered) {
							if (oldValue == null) { // created
								MapWorkspace.this.onMerged(EventType.CREATED, key, newValue, oldValue);
							} else if (newValue == null) { // removed
								MapWorkspace.this.onMerged(EventType.REMOVED, key, newValue, oldValue);
							} else {
								MapWorkspace.this.onMerged(EventType.UPDATED, key, newValue, oldValue);
							}
						}
					})
					.createOrOpen() ;
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
	
	protected WriteSession writeSession(ReadSession rsession) {
		return new MapWriteSession(this, rsession, db);
	}
	
	protected BatchSession batchSession(ReadSession rsession) {
		return new MapBatchSession(this, rsession, db);
	}

	protected ReadSession readSession() {
		return new MapReadSession(this, db);
	}

	protected <T> CompletableFuture<T> tran(WriteSession wsession, WriteJob<T> tjob, ExecutorService eservice, ExceptionHandler ehandler) {
		return CompletableFuture.supplyAsync(() -> {
			wsession.attribute(WriteJob.class, tjob);
			wsession.attribute(ExceptionHandler.class, ehandler);

			Lock wlock = rwlock.writeLock();
			try {
				wlock.tryLock(10, TimeUnit.MINUTES); // 
				T result = tjob.handle(wsession);
				
				MapWorkspace.this.dataMap.put("__endtran_", "{}");
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
		return CompletableFuture.supplyAsync(() -> {
			bsession.attribute(BatchJob.class, bjob);
			bsession.attribute(ExceptionHandler.class, ehandler);

			Lock wlock = rwlock.writeLock();
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
				
				MapWorkspace.this.dataMap.put("__endtran_", "{}");
				
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
//		return new Sequence(name, rclient.getAtomicLong(seqPrefix() + name));
		return null ;
	}

	public <T> Topic<T> topic(String name) {
//		return new Topic<T>(name, rclient.getTopic(topicPrefix() + name));
		return null ;
	}

	
	public boolean removeSelf() {
		db.getAllNames().forEach(s -> {
			if (s.startsWith(name() + ".")) {
				db.hashMap(s).createOrOpen().clear(); 
			}
		});
		
		if (central != null) central.destroySelf(); 
		listeners.clear();

		return true;
	}

	protected OutputStream outputStream(String path) {
//		RBinaryStream binaryStream = rclient.getBinaryStream(lobPrefix() + path);
//		if (binaryStream.isExists()) {
//			binaryStream.delete();
//		}
//		return binaryStream.getOutputStream();
		return null ;
	}

	protected InputStream inputStream(String path) {
//		return rclient.getBinaryStream(lobPrefix() + path).getInputStream();
		return null ;
	}

	
	private List<IndexEvent> ievents = ListUtil.newList() ;
	private boolean hasIndexer() {
		return listeners.containsKey(indexListenerId()) && central != null;
	}
	
	
	public Central central() {
		return central ;
	}
	
	
	public Workspace indexCntral(Central central) {
		this.central = central ;
		this.addListener(new NodeListener() {
			public void onChanged(EventType etype, Fqn fqn, JsonObject jvalue, JsonObject oldValue) {
				if (fqn.absPath().startsWith("/__endtran")) {
					List<IndexEvent> ies = MapWorkspace.this.ievents ;
					MapWorkspace.this.ievents = ListUtil.newList() ;
					
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
					MapWorkspace.this.ievents.add(IndexEvent.create(etype, fqn, jvalue)) ;
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
