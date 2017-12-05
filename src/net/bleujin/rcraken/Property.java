package net.bleujin.rcraken;

import net.ion.framework.parse.gson.JsonObject;

public class Property {

	private static final Property NOTFOUND = new Property(null, null, new JsonObject());
	
	private final Fqn fqn;
	private String name ;
	private final JsonObject json;
	
	public Property(Fqn fqn, String name, JsonObject json) {
		this.fqn = fqn ;
		this.name = name ;
		this.json = json ;
	}

	public static Property create(Fqn fqn, String name, JsonObject json) {
		if (json == null) return Property.NOTFOUND ;
		return new Property(fqn, name, json);
	}

	public String asString() {
		return json.asString("value");
	}

}
