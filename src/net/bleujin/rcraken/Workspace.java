package net.bleujin.rcraken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import net.bleujin.rcraken.def.Defined;
import net.bleujin.rcraken.extend.IndexEvent;
import net.bleujin.rcraken.extend.NodeListener;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.extend.Topic;
import net.bleujin.rcraken.template.TemplateFac;
import net.ion.framework.mte.Engine;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.WithinThreadExecutor;
import net.ion.nsearcher.common.WriteDocument;
import net.ion.nsearcher.config.Central;
import net.ion.nsearcher.index.Indexer;

public abstract class Workspace {

	private String wname;
	private Map<String, NodeListener> listeners = MapUtil.newMap();
	private ExecutorService es = new WithinThreadExecutor();
	private Central central;
	private Engine parseEngine;
	private TemplateFac templateFac;
	private List<IndexEvent> ievents = ListUtil.newList();

	protected Workspace(String wname) {
		this.wname = wname;
		this.parseEngine = Engine.createDefaultEngine();
		this.templateFac = new TemplateFac();
	}

	public Workspace init() {
		return this;
	}

	private JsonObject toJson(String jsonValue) {
		return jsonValue == null ? null : JsonObject.fromString(jsonValue);
	}

	protected void onMerged(EventType etype, String _fqn, String _value, String _oldValue) {
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
		return es;
	}

	public Engine parseEngine() {
		return parseEngine;
	}

	public TemplateFac templateFac() {
		return templateFac;
	}

	public Workspace executor(ExecutorService es) {
		this.es = es;
		return this;
	}

	protected abstract WriteSession writeSession(ReadSession rsession);

	protected abstract BatchSession batchSession(ReadSession rsession);

	protected abstract ReadSession readSession();

	protected abstract <T> CompletableFuture<T> tran(WriteSession wsession, WriteJob<T> tjob, ExecutorService eservice, ExceptionHandler ehandler);

	protected abstract <T> CompletableFuture<T> batch(BatchSession bsession, BatchJob<T> bjob, ExecutorService eservice, ExceptionHandler ehandler);

	public void addListener(NodeListener nodeListener) {
		listeners.put(nodeListener.id(), nodeListener);
	}

	public abstract Sequence sequence(String name);

	public abstract <T> Topic<T> topic(String name);

	public boolean removeSelf() {
		listeners.clear(); 
		if (central != null) {
			central.newIndexer().index(isession -> isession.deleteAll()) ;
			central.destroySelf(); 
		}
		return false ;
	};

	protected abstract OutputStream outputStream(String path);

	protected abstract InputStream inputStream(String path);

	protected boolean hasIndexer() {
		return listeners.containsKey(indexListenerId()) && central != null;
	}

	protected String indexListenerId() {
		return name() + ".indexer";
	}

	public Central central() {
		return central;
	}

	public Workspace indexCntral(Central central) {
		this.central = central;
		this.addListener(new NodeListener() {
			public void onChanged(EventType etype, Fqn fqn, JsonObject jvalue, JsonObject oldValue) {
				if (fqn.absPath().startsWith("/__endtran")) {
					List<IndexEvent> ies = Workspace.this.ievents;
					Workspace.this.ievents = ListUtil.newList();

					Indexer indexer = central.newIndexer();
					indexer.index(isession -> {
						for (IndexEvent ie : ies) {
							if (ie.eventType() == EventType.REMOVED) {
								isession.deleteById(ie.fqn().absPath());
								continue;
							}
							WriteDocument wdoc = isession.newDocument(ie.fqn().absPath()).keyword(Defined.Index.PARENT, ie.fqn().getParent().absPath());
							JsonObject newvalue = ie.jsonValue();
							for (String fname : newvalue.keySet()) {
								Property property = Property.create(null, ie.fqn(), fname, newvalue.asJsonObject(fname));
								property.indexTo(wdoc);
							}
							wdoc.update();
						}
						return null;
					});
					return;
				}
				if (!jvalue.keySet().isEmpty()) {
					Workspace.this.ievents.add(IndexEvent.create(etype, fqn, jvalue));
				}
			}

			@Override
			public String id() {
				return indexListenerId();
			}
		});

		return this;
	}

	public Workspace reindex(boolean clearOld) {
		if (!hasIndexer())
			throw new IllegalStateException("central not exists");
		Indexer indexer = central.newIndexer();
		indexer.index(isession -> {
			if (clearOld)
				isession.deleteAll();
			writeSession(readSession()).pathBy("/").walkBreadth().forEach(node -> {
				try {
					WriteDocument wdoc = isession.newDocument(node.fqn().absPath()).keyword(Defined.Index.PARENT, node.fqn().getParent().absPath());
					node.properties().iterator().forEachRemaining(property -> property.indexTo(wdoc));
					wdoc.update();
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			});

			return null;
		});
		return this;
	}

	public void removeListener(String id) {
		listeners.remove(id);
	}

}
