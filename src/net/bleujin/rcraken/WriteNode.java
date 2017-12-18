package net.bleujin.rcraken;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.bleujin.rcraken.Property.PType;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonElement;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.SetUtil;

public class WriteNode {

	private WriteSession wsession;
	private Fqn fqn;
	private JsonObject jsonData;

	WriteNode(WriteSession wsession, Fqn fqn, JsonObject jsonData) {
		this.wsession = wsession;
		this.fqn = fqn;
		this.jsonData = jsonData;
	}

	// property
	public WriteNode property(String name, String value) {
		JsonObject jvalue = new JsonObject().put("type", PType.String.toString()).put("value", value);
		return property(name, jvalue);
	}

	public WriteNode property(String name, long value) {
		JsonObject jvalue = new JsonObject().put("type", PType.Long.toString()).put("value", value);
		return property(name, jvalue);
	}

	public WriteNode property(String name, boolean value) {
		JsonObject jvalue = new JsonObject().put("type", PType.Boolean.toString()).put("value", value);
		return property(name, jvalue);
	}

	public WriteNode property(String name, Calendar value) {
		JsonObject jvalue = new JsonObject().put("type", PType.Date.toString()).put("value", value.getTimeInMillis());
		return property(name, jvalue);
	}

	public WriteNode property(String name, InputStream value) {
		String pvalue = fqn.absPath() + "$" + name;
		JsonObject jvalue = new JsonObject().put("type", PType.Lob.toString()).put("value", pvalue);
		wsession.attribute(pvalue, value);
		return property(name, jvalue);
	}

	public WriteNode refTo(String name, String fqn, String... fqns) {
		JsonObject jvalue = new JsonObject().put("type", PType.Ref.toString()).put("value", fqn);
		jvalue.add("values", new JsonArray());
		for (String fv : fqns) {
			jvalue.accumulate("values", fv) ;	
		}
		return property(name, jvalue);
	}
	
	public WriteNode property(String name, String value, String... values) {
		JsonObject jvalue = new JsonObject().put("type", PType.String.toString()).put("value", value) ;
		jvalue.add("values", new JsonArray());
		for (String v : values) {
			jvalue.accumulate("values", v) ;	
		}
		return property(name, jvalue);
	}
	
	public JsonObject unset(String name) {
		JsonElement removed = jsonData.remove(name) ;
		return removed.getAsJsonObject();
	}

	public WriteNode encrypt(String key, String value) throws IOException {
		return property(key, wsession.readSession().encrypt(value)) ;
	}

	// property

	private WriteNode property(String name, JsonObject jvalue) {
		jsonData.put(name, jvalue); // overwrite

		return this;
	}

	public boolean hasProperty(String name) {
		return jsonData.has(name);
	}

	public Property property(String name) {
		return Property.create(wsession.readSession(), fqn, name, jsonData.asJsonObject(name));
	}

	public Stream<Property> properties() {
		return keys().stream().map(pid->property(pid)) ;
	}
	
	public String asString(String name) {
		return property(name).asString();
	}

	public Fqn fqn() {
		return fqn;
	}

	public WriteNode clear() {
		jsonData = new JsonObject();
		return this;
	}

	public void merge() {
		wsession.merge(this, fqn, jsonData);
	}
	
	public void removeChild() {
		wsession.removeChild(this, fqn, jsonData) ;
	}

	public void removeSelf() {
		wsession.removeSelf(this, fqn, jsonData) ;
	}

	
	public WriteChildren children() {
		return new WriteChildren(wsession, fqn, childrenNames());
	}

	public WriteWalk walkBreadth() {
		List<String> fqns = ListUtil.newList() ;
		wsession.descentantBreadth(this.fqn, fqns);
		return new WriteWalk(wsession, this.fqn, fqns);
	}

	public WriteWalk walkDepth() {
		List<String> fqns = ListUtil.newList() ;
		wsession.descentantDepth(this.fqn, fqns);
		return new WriteWalk(wsession, this.fqn, fqns);
	}

	public WriteWalk refChildren(String relName, int limit) {
		List<String> fqns = ListUtil.newList() ;
		wsession.walkRef(this, relName, limit, fqns);
		return new WriteWalk(wsession, this.fqn, fqns);
	}

	
	public WriteWalk refs(String relName) {
		Set<String> relFqns = SetUtil.create(property(relName).asStrings()) ;
		for (String relFqn : relFqns.toArray(new String[0])) {
			if (! wsession.exist(relFqn)) relFqns.remove(relFqn) ;
		}
				
		return new WriteWalk(wsession, this.fqn, relFqns);
	}
	
	public WriteNode ref(String refName) {
		return wsession.pathBy(asString(refName));
	};

	
	public WriteNode parent() {
		return wsession.pathBy(fqn.getParent());
	}

	public boolean isRoot() {
		return fqn.isRoot();
	}

	public Set<String> childrenNames() {
		return wsession.readSession().readStruBy(fqn);
	}

	public WriteSession session() {
		return wsession;
	}

	public int keySize() {
		return jsonData.keySet().size();
	};

	public Set<String> keys() { // all
		return jsonData.keySet();
	}

	public boolean hasChild(String name) {
		return wsession.exist(Fqn.from(fqn, name).absPath()) ;
	};

	public WriteNode child(String name) {
		this.merge();
		return wsession.pathBy(Fqn.from(fqn, name));
	}

	public void debugPrint() {
		Debug.line(this);
	}

	public String toString() {
		return "WriteNode:[fqn:" + fqn + ", props:" + jsonData + "]";
	}





}
