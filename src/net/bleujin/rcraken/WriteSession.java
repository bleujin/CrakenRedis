package net.bleujin.rcraken;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.ObjectUtil;

public abstract class WriteSession {

	private Workspace wspace;
	private ReadSession rsession;
	private Map<String, Object> attrs = MapUtil.newMap();

	protected WriteSession(Workspace wspace, ReadSession rsession) {
		this.wspace = wspace;
		this.rsession = rsession;
	}

	public ReadSession readSession() {
		return rsession;
	}

	public WriteNode root() {
		return pathBy("/");
	}

	public WriteNode pathBy(String path) {
		Fqn fqn = Fqn.from(path);
		return pathBy(fqn);
	}

	public WriteNode pathBy(String... elements) {
		return pathBy(Fqn.fromElements(elements));
	}

	public WriteNode pathBy(Fqn fqn) {
		return new WriteNode(this, fqn, ObjectUtil.coalesce(readDataBy(fqn), new JsonObject()));
	}

	protected abstract void merge(WriteNode wnode, Fqn fqn, JsonObject data);

	protected abstract void removeChild(WriteNode wnode, Fqn fqn, JsonObject data);
	
	protected abstract void removeSelf(WriteNode wnode, Fqn fqn, JsonObject data);
	
	public void copySelf(WriteNode sourceNode, Fqn sourceFqn, JsonObject sourceData, String destPath) {
		Fqn dest = Fqn.from(destPath);
		sourceNode.children().forEach(wn -> {
			// if (exist(destPath)) throw new IllegalStateException("destPath already exist
			// :" + destPath) ;
			wn.copySelf(Fqn.from(dest, wn.fqn().name()).absPath());
		});
		merge(pathBy(destPath), dest, sourceData);
	}

	public boolean hasAttribute(String name) {
		return attrs.containsKey(name);
	}

	public <T> void attribute(Class<T> clz, T obj) {
		attrs.put(clz.getCanonicalName(), obj);
	}

	public <T> T attribute(Class<T> clz) {
		return (T) attrs.get(clz.getCanonicalName());
	}

	public void endTran() {
		attrs.clear();
	}

	public Map<String, Object> attrs() {
		return attrs;
	}

	public Workspace workspace() {
		return wspace;
	}

	public WriteSession attribute(String name, Object value) {
		attrs.put(name, value);
		return this;
	}

	protected abstract Set<String> readStruBy(Fqn fqn);

	protected abstract JsonObject readDataBy(Fqn fqn);

	void descentantBreadth(Fqn fqn, List<String> fqns, int depth) {
		if (depth <= 0) return ;
		
		for(String childName : readStruBy(fqn)) {
			Fqn child = Fqn.from(fqn, childName);
			fqns.add(child.absPath()) ;
			descentantBreadth(child, fqns, --depth);
		}
	}

	void descentantDepth(Fqn fqn, List<String> fqns, int depth) {
		if (depth <= 0) return ;
		
		for(String childName : readStruBy(fqn)) {
			Fqn child = Fqn.from(fqn, childName);
			fqns.add(child.absPath()) ;
		}

		for(String childName : readStruBy(fqn)) {
			descentantDepth(Fqn.from(fqn, childName), fqns, --depth);
		}
	}

	public abstract boolean exist(String path);

	public void walkRef(WriteNode source, String relName, int limit, List<String> fqns) {
		if (limit == 0)
			return;
		for (String relPath : source.property(relName).asStrings()) {
			Fqn rel = Fqn.from(relPath);
			if (!source.session().exist(rel.absPath()))
				continue;
			fqns.add(rel.absPath());
			walkRef(source.session().pathBy(rel), relName, --limit, fqns);
		}
	}


}
