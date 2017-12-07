package net.bleujin.rcraken;

import java.util.Calendar;

import net.bleujin.rcraken.def.Defined;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.DateUtil;

public class Property  {

	private static final Property NOTFOUND = new Property(null, null, new JsonObject());
	
	private final Fqn fqn;
	private String name ;
	private final JsonObject json;
	
	
	public enum PType{
		String, Long, Date, Boolean, Ref, Lob
	}
	
	
	
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
		return json.asString(Defined.Property.Value);
	}
	
	public long asLong() {
		return json.asLong(Defined.Property.Value);
	}
	
	public int asInt() {
		return json.asInt(Defined.Property.Value);
	}
	
	
	public Calendar asDate() {
		return DateUtil.longToCalendar(json.asLong(Defined.Property.Value));
	}
	
	public boolean asBoolean() {
		return json.asBoolean(Defined.Property.Value) ;
	}
	
	
	

	public boolean isExist() {
		return fqn != null ;
	}

	
	public PType type() {
		return PType.valueOf(json.asString(Defined.Property.Type)) ;
	}
}
