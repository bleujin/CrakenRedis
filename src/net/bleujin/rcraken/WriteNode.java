package net.bleujin.rcraken;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import net.bleujin.rcraken.Property.PType;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonElement;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.DateUtil;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.NumberUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.SetUtil;
import net.ion.framework.util.StringUtil;

public class WriteNode implements CommonNode, Comparable<WriteNode> {

	private WriteSession wsession;
	private Fqn fqn;
	private JsonObject jsonData;

	WriteNode(WriteSession wsession, Fqn fqn, JsonObject jsonData) {
		this.wsession = wsession;
		this.fqn = fqn;
		this.jsonData = JsonObject.fromString(jsonData.toString()) ; // clone new jsonObject
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
	
	public WriteNode property(String name, double value) {
		JsonObject jvalue = new JsonObject().put("type", PType.Double.toString()).put("value", value);
		return property(name, jvalue);
	}
	
	
	


	public WriteNode property(String name, InputStream value) {
		String pvalue = fqn.absPath() + "$" + name;
		JsonObject jvalue = new JsonObject().put("type", PType.Lob.toString()).put("value", pvalue);
		wsession.attribute(pvalue, value);
		return property(name, jvalue);
	}

	public WriteNode refTos(String refName, String refTarget, String... refTargets) { 
		if (hasProperty(refName)) {
			JsonObject jvalue = jsonData.asJsonObject(refName) ;
			JsonArray jarray = jvalue.has("values") ? jvalue.asJsonArray("values") : new JsonArray() ;
			jarray.adds(refTarget) ; 
			jvalue.add("values", jarray);
		} else {
			JsonObject jvalue = new JsonObject().put("type", PType.Ref.toString()).put("value", refTarget);
			jvalue.add("values", new JsonArray());
			jvalue.accumulate("values", refTarget) ;
			for (String fv : refTargets) {
				jvalue.accumulate("values", fv) ;	
			}
			property(refName, jvalue);
		}
		return this ;
	}
	
	public WriteNode refTo(String refName, String refTarget, String... refTargets) { // refTo accumulate
		this.unset(refName) ;
		refTo(refName, refTarget, refTargets) ;
		
		return this ;
	}
	
	public WriteNode property(String name, String... values) {
		return property(name, values[0], ArrayUtil.newSubArray(values, 1, values.length)) ;
	}
	
	private WriteNode property(String name, String value, String... values) {
		JsonObject jvalue = new JsonObject().put("type", PType.String.toString()).put("value", value) ;
		jvalue.add("values", new JsonArray());
		for (String v : values) {
			jvalue.accumulate("values", v) ;	
		}
		return property(name, jvalue);
	}
	
	public WriteNode append(String name, String... values) {
		if (hasProperty(name)) {
			JsonObject jvalue = jsonData.asJsonObject(name) ;
			JsonArray jarray = jvalue.has("values") ? jvalue.asJsonArray("values") : new JsonArray() ;
			jarray.adds(values) ;
			jvalue.add("values", jarray);
		} else {
			property(name, values) ;
		}
		return this;
	}

	
	public WriteNode increase(String name) {
		long current = property(name).asLong() ;
		return property(name, ++current) ;
	}

	
	public JsonObject unset(String name) {
		JsonElement removed = jsonData.remove(name) ;
		
		return removed == null ? JsonObject.create() : removed.getAsJsonObject();
	}

	public WriteNode unsetWith(String name) {
		JsonElement removed = jsonData.remove(name) ;
		return this;
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
	
	public Object asValue(String name) {
		return property(name).value() ;
	}
	
	public <T> T defaultValue(String name, T dftValue) {
		return (T) ObjectUtil.coalesce(asValue(name), dftValue) ;
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

	public void copySelf(String destPath) {
		wsession.copySelf(this, fqn, jsonData, destPath) ;
	}
	
	public void moveSelf(String destPath) {
		wsession.copySelf(this, fqn, jsonData, destPath) ;
		removeSelf();
	}



	
	
	public WriteChildren children() {
		return new WriteChildren(wsession, fqn, childrenNames());
	}

	public WriteWalk walkBreadth() {
		return walkBreadth(false, 10) ;
	}

	public WriteWalk walkDepth() {
		return walkDepth(false, 10) ;
	}

	public WriteWalk walkBreadth(boolean includeSelf, int maxlevel) {
		List<String> fqns = ListUtil.newList() ;
		if (includeSelf) fqns.add(fqn().absPath()) ;
		
		wsession.descentantBreadth(this.fqn, fqns, maxlevel);
		return new WriteWalk(wsession, this.fqn, fqns);
	}

	public WriteWalk walkDepth(boolean includeSelf, int maxlevel) {
		List<String> fqns = ListUtil.newList() ;
		if (includeSelf) fqns.add(fqn().absPath()) ;
		
		wsession.descentantDepth(this.fqn, fqns, maxlevel);
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

	@Override
	public boolean hasRef(String refName) {
		return wsession.readSession().pathBy(asString(refName)).exist() ;
	}

	
	public WriteNode parent() {
		this.merge(); 
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



	@Override
	public int compareTo(WriteNode o) {
		return fqn.absPath().compareTo(o.fqn.absPath());
	}

	public ReadNode toReadNode() {
		return wsession.readSession().pathBy(fqn);
	}

	public WriteNode changeValue(String pid, String stringValue) {
		if (property(pid).type() == PType.String) {
			property(pid, stringValue) ;
		} else if (property(pid).type() == PType.Long) {
			property(pid, NumberUtil.toLong(stringValue)) ;
		} else if (property(pid).type() == PType.Date) {
			property(pid, DateUtil.stringToCalendar(stringValue)) ;
		} else if (property(pid).type() == PType.Boolean) {
			property(pid, Boolean.getBoolean(stringValue)) ;
		} else if (property(pid).type() == PType.Ref && StringUtil.split(stringValue, ", ").length > 0) {
			String[] values = StringUtil.split(stringValue, ", ") ;
			refTo(pid, values[0], values.length > 1 ? (String[])ArrayUtil.subarray(values, 1, values.length) : new String[0]) ;
		} else {
			throw new IllegalArgumentException("known property type") ;
		}
		return this;
	}




}
