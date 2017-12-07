package net.bleujin.rcraken;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.redisson.api.RMap;

import net.bleujin.rcraken.Property.PType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;

public class WriteNode {

	private WriteSession wsession;
	private Fqn fqn;
	private JsonObject data;

	WriteNode(WriteSession wsession, Fqn fqn, JsonObject data) {
		this.wsession = wsession;
		this.fqn = fqn;
		this.data = data;
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
		JsonObject jvalue = new JsonObject().put("type", PType.Boolean.toString()).put("value", value.getTimeInMillis());
		return property(name, jvalue);
	}

	public WriteNode property(String name, InputStream value) {
		String pvalue = fqn.absPath() + "$" + name;
		JsonObject jvalue = new JsonObject().put("type", PType.Lob.toString()).put("value", pvalue);
		wsession.attribute(pvalue, value);
		return property(name, jvalue);
	}

	public WriteNode refTo(String name, String fqn) {
		JsonObject jvalue = new JsonObject().put("type", PType.Ref.toString()).put("value", fqn);
		return property(name, jvalue);
	}
	// property

	private WriteNode property(String name, JsonObject jvalue) {
		data.put(name, jvalue); // overwrite

		return this;
	}

	public boolean hasProperty(String name) {
		return data.has(name);
	}

	public Property property(String name) {
		return Property.create(wsession.readSession(), fqn, name, data.asJsonObject(name));
	}

	public Stream<Property> properties() {
		Iterator<String> keyIter = keys().iterator() ;
		return StreamSupport.stream(Spliterators.spliterator(new Iterator<Property>() {
			@Override
			public boolean hasNext() {
				return keyIter.hasNext();
			}

			@Override
			public Property next() {
				return property(keyIter.next());
			}
		}, keySize(), 0), false);
	}
	
	public String asString(String name) {
		return property(name).asString();
	}

	public Fqn fqn() {
		return fqn;
	}

	public WriteNode clear() {
		data = new JsonObject();
		return this;
	}

	public void merge() {
		wsession.merge(this, fqn, data);
	}
	
	public void removeChild() {
		wsession.removeChild(this, fqn, data) ;
	}

	public void removeSelf() {
		wsession.removeSelf(this, fqn, data) ;
	}


	public WriteChildren children() {
		return new WriteChildren(wsession, fqn, childrenNames());
	}

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
		return data.keySet().size();
	};

	public Set<String> keys() { // all
		return data.keySet();
	}

	public boolean hasChild(String name) {
		return data.has(name);
	};

	public WriteNode child(String name) {
		return wsession.pathBy(Fqn.from(fqn, name));
	}

	public void debugPrint() {
		Debug.line(this);
	}

	public String toString() {
		return "WriteNode:[fqn:" + fqn + ", props:" + data + "]";
	}



}
