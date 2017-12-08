package net.bleujin.rcraken;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Calendar;
import java.util.List;

import net.bleujin.rcraken.def.Defined;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.DateUtil;
import net.ion.framework.util.ListUtil;

public class Property {

	private static final Property NOTFOUND = new Property(null, null, "N/A", JsonObject.create());

	private final ReadSession rsession;
	private final Fqn fqn;
	private final String name;
	private final JsonObject json;

	public enum PType {
		String, Long, Date, Boolean, Ref, Lob
	}

	public Property(ReadSession rsession, Fqn fqn, String name, JsonObject json) {
		this.rsession = rsession ;
		this.fqn = fqn;
		this.name = name;
		this.json = json;
	}

	public static Property create(ReadSession rsession, Fqn fqn, String name, JsonObject json) {
		if (json == null)
			return Property.NOTFOUND;
		return new Property(rsession, fqn, name, json);
	}

	public String asString() {
		return json.asString(Defined.Property.Value);
	}

	public String[] asStrings() {
		if (asString() == null) return new String[0] ;
		List<String> result = ListUtil.newList() ;
		result.add(asString()) ;
		if (json.has("values")) json.asJsonArray("values").spliterator().forEachRemaining(e -> result.add(e.getAsString()));;
		
		return result.toArray(result.toArray(new String[0])) ; 
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
		return json.asBoolean(Defined.Property.Value);
	}

	public InputStream asStream() {
		if (PType.Lob.name().equals(json.asString(Defined.Property.Type))) {
			return rsession.workspace().inputStream(asString()) ;
		} else {
			return new StringBufferInputStream(asString()) ;
		}
	}

	public boolean isExist() {
		return this != NOTFOUND;
	}

	public PType type() {
		return PType.valueOf(json.asString(Defined.Property.Type));
	}
	
	public boolean equals(Object o) {
		if (o instanceof Property) {
			Property that = (Property) o ;
			return this.fqn.equals(that.fqn) && this.name.equals(that.name) ;
		} return false ;
	}
	
	public String toString() {
		return "Property["  + name + ", " + json + "]" ;
	}


}
