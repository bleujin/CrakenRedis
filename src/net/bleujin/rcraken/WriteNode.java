package net.bleujin.rcraken;

import org.redisson.api.RMap;

import net.ion.framework.parse.gson.JsonObject;

public class WriteNode {

	private WriteSession wsession;
	private Fqn fqn;
	private RMap<String, String> cmap;

	WriteNode(WriteSession wsession, Fqn fqn, RMap<String, String> cmap) {
		this.wsession = wsession ;
		this.fqn = fqn ;
		this.cmap = cmap ;
	}

	public WriteNode property(String name, String value) {
		JsonObject json = dataBy() ;
		JsonObject jvalue = new JsonObject().put("type", "String").put("value", value) ;
		json.put(name, jvalue) ; // overwrite
		
		cmap.fastPut(fqn.absPath(), json.toString()) ;
		
		return this ;
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
		if (cmap.containsKey(fqn.absPath())) {
			String jsonString = cmap.get(fqn.absPath()) ;
			return JsonObject.fromString(jsonString) ;	
		} else {
			return new JsonObject() ;
		}
	}


}
