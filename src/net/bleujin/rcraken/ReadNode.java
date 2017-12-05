package net.bleujin.rcraken;

import net.ion.framework.parse.gson.JsonObject;

public class ReadNode {

	private ReadSession rsession;
	private Fqn fqn;
	private JsonObject data;

	public ReadNode(ReadSession rsession, Fqn fqn, JsonObject data) {
		this.rsession = rsession ;
		this.fqn = fqn ;
		this.data = data ;
	}
	
	
	public boolean hasProperty(String name) {
		return data.has(name) ;
	}
	
	public Property property(String name) {
		return Property.create(fqn, name, data.asJsonObject(name)) ;
	}

	public String asString(String name) {
		return property(name).asString() ;
	}

	public Fqn fqn() {
		return fqn;
	}

	
	

	public ReadNode children() {
		return null;
	}


	
}
