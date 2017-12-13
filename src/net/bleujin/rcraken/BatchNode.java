package net.bleujin.rcraken;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.bleujin.rcraken.Property.PType;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonElement;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;

public class BatchNode {

	private BatchSession bsession;
	private Fqn fqn;
	private JsonObject jsonData;

	public BatchNode(BatchSession bsession, Fqn fqn, JsonObject jsonData) {
		this.bsession = bsession ;
		this.fqn = fqn ;
		this.jsonData = jsonData ;
	}

	

	// property
	public BatchNode property(String name, String value) {
		JsonObject jvalue = new JsonObject().put("type", PType.String.toString()).put("value", value);
		return property(name, jvalue);
	}

	public BatchNode property(String name, long value) {
		JsonObject jvalue = new JsonObject().put("type", PType.Long.toString()).put("value", value);
		return property(name, jvalue);
	}

	public BatchNode property(String name, boolean value) {
		JsonObject jvalue = new JsonObject().put("type", PType.Boolean.toString()).put("value", value);
		return property(name, jvalue);
	}

	public BatchNode property(String name, Calendar value) {
		JsonObject jvalue = new JsonObject().put("type", PType.Boolean.toString()).put("value", value.getTimeInMillis());
		return property(name, jvalue);
	}

	public BatchNode refTo(String name, String fqn, String... fqns) {
		JsonObject jvalue = new JsonObject().put("type", PType.Ref.toString()).put("value", fqn);
		jvalue.add("values", new JsonArray());
		for (String fv : fqns) {
			jvalue.accumulate("values", fv) ;	
		}
		return property(name, jvalue);
	}
	
	public BatchNode property(String name, String value, String... values) {
		JsonObject jvalue = new JsonObject().put("type", PType.String.toString()).put("value", value) ;
		jvalue.add("values", new JsonArray());
		for (String v : values) {
			jvalue.accumulate("values", v) ;	
		}
		return property(name, jvalue);
	}
	
	// property

	private BatchNode property(String name, JsonObject jvalue) {
		jsonData.put(name, jvalue); // overwrite

		return this;
	}

	public Fqn fqn() {
		return fqn;
	}

	public BatchNode clear() {
		jsonData = new JsonObject();
		return this;
	}

	public void insert() {
		bsession.insert(this, fqn, jsonData);
	}

	public BatchSession session() {
		return bsession;
	}

	public String toString() {
		return "BatchNode:[fqn:" + fqn + ", props:" + jsonData + "]";
	}

	
}
