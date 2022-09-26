package net.bleujin.rcraken.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ecs.html.Map;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.MapModificationListener;
import org.mapdb.Serializer;

import com.google.common.util.concurrent.Futures;

import net.bleujin.rcraken.BatchJob;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.ExceptionHandler;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.extend.MapSequence;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.extend.Topic;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;

public class MapWorkspace extends Workspace{

	
	private AtomicBoolean inited = new AtomicBoolean(false) ;
	private HTreeMap<String, String> dataMap;
	private DB db;
	private ReadWriteLock rwlock;
	private HTreeMap<String, byte[]> binaryData;
	
	public MapWorkspace(CrakenNode cnode, String wname, MapNode mnode, DB db) {
		super(cnode, wname) ;
		this.db = db;
		this.rwlock = mnode.rwLock(wname + ".rwlock");
		this.binaryData = db.hashMap(lobMapName()).keySerializer(Serializer.STRING).valueSerializer(Serializer.BYTE_ARRAY).createOrOpen() ;
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

	String nodeMapName(){
		return name() + ".node" ;
	}
	
	String struMapName() {
		return name() + ".stru";
	}

	String lobMapName(){
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
		return new MapReadSession(node(), this, db);
	}

	protected <T> CompletableFuture<T> tran(WriteSession wsession, WriteJob<T> tjob, ExecutorService eservice, ExceptionHandler ehandler) {
		if (eservice.isTerminated() || eservice.isShutdown()) return CompletableFuture.completedFuture(null) ;
		
		return CompletableFuture.supplyAsync(() -> {
			wsession.attribute(WriteJob.class, tjob);
			wsession.attribute(ExceptionHandler.class, ehandler);

			Lock wlock = rwlock.writeLock();
			try {
				wlock.tryLock(10, TimeUnit.MINUTES); //
				T result = tjob.handle(wsession);
				
				MapWorkspace.this.dataMap.put("__endtran_", "{}");
				db.commit();
				wsession.endTran();
				return result;
			} catch (Throwable ex) {
				ehandler.handle(wsession, tjob, ex);
				db.rollback();
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

			Lock wlock = rwlock.writeLock();
			try {
				wlock.tryLock(10, TimeUnit.MINUTES); //
				T result = bjob.handle(bsession);

				MapWorkspace.this.dataMap.put("__endtran_", "{}");
				db.commit(); 
				bsession.endTran();
				return result ;
			} catch (Throwable ex) {
				ehandler.handle(bsession, bjob, ex);
				db.rollback();
				throw new IllegalStateException(ex) ; 
			} finally {
				wlock.unlock(); 
			}
		}, eservice) ;
	}


	public Sequence sequence(String name) {
		String seqName = seqPrefix() + name;
		return MapSequence.create(db.atomicLong(seqName).createOrOpen(), seqName) ;
	}

	public <T> Topic<T> topic(String name) { 
		throw new UnsupportedOperationException("current fn not supported in mapdb-store") ;
	}

	
	public boolean removeSelf() throws IOException {
		super.removeSelf() ;
		db.getAllNames().forEach(s -> {
			if (s.startsWith(name() + ".")) {
				Object e = db.get(s) ;
				if (Map.class.isInstance(e)) {
					db.hashMap(s).createOrOpen().clear(); 
				} else if (Atomic.Long.class.isInstance(e)) {
					((Atomic.Long)e).set(0);
				}
			}
		});
		
		return true;
	}

	protected OutputStream outputStream(String path) {
		return new OutputStream() {
			private ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
			public void write(int b) throws IOException {
				bout.write(b);
			}
			public void close() throws IOException {
				MapWorkspace.this.binaryData.put(path, bout.toByteArray()) ;
			}
		} ;
	}

	
	protected InputStream inputStream(String path) { 
		byte[] bytes = binaryData.get(path) ;
		ByteArrayInputStream binput = new ByteArrayInputStream(bytes) ;
		return new InputStream() {
			public int read() throws IOException {
				return binput.read();
			}
		} ;
	}
}
