package net.bleujin.rcraken.store.infinispan;

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

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.ecs.html.Map;
import org.infinispan.Cache;
import org.infinispan.filter.KeyFilter;
import org.infinispan.metadata.Metadata;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.impl.EventImpl;
import org.infinispan.notifications.cachelistener.filter.CacheEventFilter;
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
import net.bleujin.rcraken.extend.ISpanSequence;
import net.bleujin.rcraken.extend.MapSequence;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.extend.Topic;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;

public class ISpanWorkspace extends Workspace{

	
	private AtomicBoolean inited = new AtomicBoolean(false) ;
	private HTreeMap<String, byte[]> binaryData;
	private Cache<String, String> nmap;
	
	public ISpanWorkspace(CrakenNode cnode, String wname, ISpanNode mnode, Cache<String, String> nmap) {
		super(cnode, wname) ;
		this.nmap = nmap ;
	}

	public Workspace init() {
		if (!inited.getAndSet(true)) {
			nmap.start(); 
			nmap.addListener(new WorkListener(this), new KeyFilter<String>() {
				@Override
				public boolean accept(String key) {
					return ! key.endsWith("/");
				}
			});
		}
		return this;
	}
	
	@Listener (clustered = true)
	public static class WorkListener {
		private ISpanWorkspace iworkspace;
		public WorkListener(ISpanWorkspace iworkspace) {
			this.iworkspace = iworkspace ;
		}

		@CacheEntryCreated
		public void entryCreated(CacheEntryCreatedEvent<String, String> event) {
			if (event.isPre()) return ; 
			iworkspace.onMerged(EventType.CREATED, event.getKey(), event.getValue(), null);
		}

		@CacheEntryModified
		public void entryModified(CacheEntryModifiedEvent<String, String> event) {
			EventImpl<String, String> ievent = (EventImpl)event ;
			if (ievent.getOldValue() == null) {
				iworkspace.onMerged(EventType.CREATED, event.getKey(), event.getValue(), null);
			} else {
				iworkspace.onMerged(EventType.UPDATED, event.getKey(), event.getValue(), ievent.getOldValue());
			}
		}
		
		@CacheEntryRemoved
		public void entryRemoved(CacheEntryRemovedEvent<String, String> event) {
			iworkspace.onMerged(EventType.REMOVED, event.getKey(), null, ((EventImpl<String, String>)event).getOldValue());
		}
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
		return new ISpanWriteSession(this, rsession, nmap);
	}
	
	protected BatchSession batchSession(ReadSession rsession) {
		return new ISpanBatchSession(this, rsession, nmap);
	}

	protected ReadSession readSession() {
		return new ISpanReadSession(node(), this, nmap);
	}

	protected <T> CompletableFuture<T> tran(WriteSession wsession, WriteJob<T> tjob, ExecutorService eservice, ExceptionHandler ehandler) {
		if (eservice.isTerminated() || eservice.isShutdown()) return CompletableFuture.completedFuture(null) ;
		
		return CompletableFuture.supplyAsync(() -> {
			wsession.attribute(WriteJob.class, tjob);
			wsession.attribute(ExceptionHandler.class, ehandler);

			TransactionManager transactionManager = nmap.getAdvancedCache().getTransactionManager();
			try {
				transactionManager.begin();
				T result = tjob.handle(wsession);
				
				transactionManager.commit();
				wsession.endTran();
				return result;
			} catch (Throwable ex) {
				ehandler.handle(wsession, tjob, ex);
				try {
					transactionManager.rollback();
				} catch (IllegalStateException | SecurityException | SystemException ex1) {
					throw new IllegalStateException(ex1) ; 
				}
				throw new IllegalStateException(ex) ; 
			} 
		}, eservice) ;
	}



	
	protected <T> CompletableFuture<T> batch(BatchSession bsession, BatchJob<T> bjob, ExecutorService eservice, ExceptionHandler ehandler) {
		if (eservice.isTerminated() || eservice.isShutdown()) return CompletableFuture.completedFuture(null) ;
		
		return CompletableFuture.supplyAsync(() -> {
			bsession.attribute(BatchJob.class, bjob);
			bsession.attribute(ExceptionHandler.class, ehandler);

			TransactionManager transactionManager = nmap.getAdvancedCache().getTransactionManager();
			try {
				transactionManager.begin();
				T result = bjob.handle(bsession);

				transactionManager.commit(); 
				bsession.endTran();
				return result ;
			} catch (Throwable ex) {
				ehandler.handle(bsession, bjob, ex);
				try {
					transactionManager.rollback();
				} catch (IllegalStateException | SecurityException | SystemException ex1) {
					throw new IllegalStateException(ex1) ; 
				}
				throw new IllegalStateException(ex) ; 
			} finally {
			}
		}, eservice) ;
	}


	public Sequence sequence(String name) {
		String seqName = seqPrefix() + name;
		return new ISpanSequence(seqName, nmap) ;
	}

	public <T> Topic<T> topic(String name) { 
		throw new UnsupportedOperationException("current fn not supported in mapdb-store") ;
	}

	
	public boolean removeSelf() {
		super.removeSelf() ;
		nmap.clear();
		
		return true;
	}

	protected OutputStream outputStream(String path) {
		return new OutputStream() {
			private ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
			public void write(int b) throws IOException {
				bout.write(b);
			}
			public void close() throws IOException {
				ISpanWorkspace.this.binaryData.put(path, bout.toByteArray()) ;
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
