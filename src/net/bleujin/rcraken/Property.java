package net.bleujin.rcraken;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import net.bleujin.rcraken.def.Defined;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonPrimitive;
import net.ion.framework.parse.gson.internal.LazilyParsedNumber;
import net.ion.framework.util.DateUtil;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.SetUtil;
import net.ion.nsearcher.common.WriteDocument;

public class Property {

	private static final Property NOTFOUND = new Property(null, null, "N/A", JsonObject.create());

	private final ReadSession rsession;
	private final Fqn fqn;
	private final String name;
	private final JsonObject json;

	public enum PType {
		String {
			public void indexTo(WriteDocument wdoc, Property property) {
				Arrays.asList(property.asStrings()).forEach(str -> wdoc.keyword(property.name, str));
			}
		}, Long {
			public void indexTo(WriteDocument wdoc, Property property) {
				wdoc.number(property.name, property.asLong()) ;
			}
		}, Date {
			public void indexTo(WriteDocument wdoc, Property property) {
				wdoc.date(property.name, property.asDate().getTime()) ;
			}
		}, Boolean {
			public void indexTo(WriteDocument wdoc, Property property) {
				wdoc.keyword(property.name, property.asString()) ;
			}
		}, Ref {
			public void indexTo(WriteDocument wdoc, final Property property) {
				Arrays.asList(property.asStrings()).forEach(str -> wdoc.keyword(property.name, str));
			}
		}, Lob {
			public void indexTo(WriteDocument wdoc, Property property) {
			}
		}, Unknown {
			public void indexTo(WriteDocument wdoc, Property property) {
				
			}
		};
		
		
		public abstract void indexTo(WriteDocument wdoc, Property property)  ;
		
		public static PType from(String typeName) {
			if ("String".equalsIgnoreCase(typeName)) {
				return String ;
			} else if ("Long".equalsIgnoreCase(typeName)) {
				return Long ;
			} else if ("Date".equalsIgnoreCase(typeName)) {
				return Date ;
			} else if ("Boolean".equalsIgnoreCase(typeName)) {
				return Boolean ;
			} else if ("Ref".equalsIgnoreCase(typeName)) {
				return Ref ;
			} else {
				return Unknown ;
			}
		}
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

	public String name() {
		return name ;
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

	public Set<Object> asSet() {
		if (asString() == null) return SetUtil.EMPTY ;
		Set<Object> result = SetUtil.newSet() ;
		result.add(asString()) ;
		if (json.has("values")) json.asJsonArray("values").spliterator().forEachRemaining(e -> result.add(e.getAsJsonPrimitive().getValue()));;
		
		return result ; 
	}

	public <T> T defaultValue(T defaultValue) {
		return (T) ObjectUtil.coalesce(value(), defaultValue);
	}
	
	public Object value() {
		JsonPrimitive value = json.getAsJsonPrimitive(Defined.Property.Value);
		if (value == null) return null ;
		Object result = value.getValue() ;
		if (result instanceof LazilyParsedNumber) {
			if (type() == PType.Date) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(((LazilyParsedNumber)result).longValue());
				return cal ;
			}
			return ((LazilyParsedNumber)result).longValue() ;
		}
		return result;
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
			if (asString() == null) throw new IllegalStateException("not found property :" + name) ;
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

	public void indexTo(WriteDocument wdoc) {
		this.type().indexTo(wdoc, this);
	}




}
