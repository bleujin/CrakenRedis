package net.bleujin.rcraken;

import org.redisson.api.RMap;

import net.ion.framework.parse.gson.JsonObject;

public class WriteNode {

	private WriteSession wsession;
	private Fqn fqn;
	private RMap<String, String> dataMap;

	WriteNode(WriteSession wsession, Fqn fqn, RMap<String, String> dataMap) {
		this.wsession = wsession ;
		this.fqn = fqn ;
		this.dataMap = dataMap ;
	}

	public WriteNode property(String name, String value) {
		JsonObject jvalue = new JsonObject().put("type", "String").put("value", value) ;
		property(name, jvalue);
		
		return this ;
	}

	public WriteNode property(String name, long value) {
		JsonObject jvalue = new JsonObject().put("type", "Long").put("value", value) ;
		property(name, jvalue);
		
		return this;
	}

	private void property(String name, JsonObject jvalue) {
		JsonObject json = dataBy() ;
		json.put(name, jvalue) ; // overwrite
		
		dataMap.fastPut(fqn.absPath(), json.toString()) ;
	}
	
	
	
	public boolean hasProperty(String name) {
		return dataBy().has(name) ;
	}
	
	public Property property(String name) {
		return Property.create(fqn, name, dataBy().asJsonObject(name)) ;
	}

	public String asString(String name) {
		return property(name).asString() ;
	}

	public Fqn fqn() {
		return fqn;
	}
	
	private JsonObject dataBy() {
		String jsonString = dataMap.get(fqn.absPath()) ;
		return JsonObject.fromString(jsonString) ;	
	}



}
