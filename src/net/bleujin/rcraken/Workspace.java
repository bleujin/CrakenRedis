package net.bleujin.rcraken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import net.bleujin.rcraken.def.Defined;
import net.bleujin.rcraken.extend.CDDHandler;
import net.bleujin.rcraken.extend.CDDModifiedEvent;
import net.bleujin.rcraken.extend.CDDRemovedEvent;
import net.bleujin.rcraken.extend.IndexEvent;
import net.bleujin.rcraken.extend.NodeListener;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.extend.Topic;
import net.bleujin.rcraken.template.TemplateFac;
import net.bleujin.searcher.SearchController;
import net.bleujin.searcher.common.WriteDocument;
import net.ion.framework.mte.Engine;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.WithinThreadExecutor;

public abstract class Workspace {

	private CrakenNode cnode;
	private String wname;
	private Map<String, NodeListener> listeners = MapUtil.newMap();
	private ExecutorService es = new WithinThreadExecutor();
	private SearchController central;
	private Engine parseEngine;
	private TemplateFac templateFac;
	private List<IndexEvent> ievents = ListUtil.newList();

	protected Workspace(CrakenNode cnode, String wname) {
		this.cnode = cnode;
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

	public void add(CDDHandler cddHandler) {
		addListener(new NodeListener() {
			@Override
			public String id() {
				return cddHandler.id();
			}

			@Override
			public void onChanged(EventType etype, Fqn fqn, JsonObject value, JsonObject oldValue) {
				if (fqn.isPattern(cddHandler.pathPattern())) {
					Map<String, String> resolveMap = fqn.resolve(cddHandler.pathPattern());
					final AtomicReference<WriteJobNoReturn> referChain = new AtomicReference<WriteJobNoReturn>();
					if (etype == EventType.REMOVED) {
						referChain.set(cddHandler.deleted(resolveMap, CDDRemovedEvent.create(readSession(), fqn, ObjectUtil.coalesce(value, oldValue))));
					} else if (etype == EventType.UPDATED) {
						referChain.set(cddHandler.modified(resolveMap, CDDModifiedEvent.create(readSession(), fqn, value, oldValue)));
					} else if (etype == EventType.CREATED) {
						referChain.set(cddHandler.modified(resolveMap, CDDModifiedEvent.create(readSession(), fqn, value, oldValue)));
					}

					if (referChain.get() != null) {
						readSession().tran(wsession -> {
							referChain.get().handle(wsession);
							return null;
						});
					}
				}
			}

		});
	}

	public abstract Sequence sequence(String name);

	public abstract <T> Topic<T> topic(String name);

	public boolean removeSelf() throws IOException {
		listeners.clear();
		if (central != null) {
			central.index(isession -> isession.deleteAll());
			central.destroySelf();
		}
		return false;
	};

	protected abstract OutputStream outputStream(String path) throws IOException;

	protected abstract InputStream inputStream(String path) throws IOException ;

	protected boolean hasIndexer() {
		return listeners.containsKey(indexListenerId()) && central != null;
	}

	protected String indexListenerId() {
		return name() + ".indexer";
	}

	public SearchController central() {
		return Optional.ofNullable(central).orElseThrow(() -> new IllegalStateException("central is blank. set central in workspace"));
	}

	public Workspace indexCntral(SearchController central) {
		this.central = central;
		this.addListener(new NodeListener() {
			public void onChanged(EventType etype, Fqn fqn, JsonObject jvalue, JsonObject oldValue) {
				if (fqn.absPath().startsWith("/__endtran")) {
					List<IndexEvent> ies = Workspace.this.ievents;
					Workspace.this.ievents = ListUtil.newList(); // reset

					try {
						central.index(isession -> {
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
					} catch (IOException ex) {
						throw new IllegalStateException(ex);
					}

					return;
				}
				if (jvalue != null && !jvalue.keySet().isEmpty()) {
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
		try {
			central.index(isession -> {
				if (clearOld)
					isession.deleteAll();
				
				for (WriteNode node : writeSession(readSession()).pathBy("/").walkBreadth()) {
					WriteDocument wdoc = isession.newDocument(node.fqn().absPath()).keyword(Defined.Index.PARENT, node.fqn().getParent().absPath());
					node.properties().iterator().forEachRemaining(property -> property.indexTo(wdoc));
					wdoc.update();
				}
				return null;
			});
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		return this;
	}

	public void removeListener(String id) {
		listeners.remove(id);
	}

	public CrakenNode node() {
		return cnode;
	}

}
