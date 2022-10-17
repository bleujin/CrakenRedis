package net.bleujin.rcraken.template;

import java.util.Map;

import net.ion.framework.util.StringUtil;

public class PropertyValue {

	private String type;
	private Object value;

	private PropertyValue(Object type, Object value) {
		this.type = StringUtil.toString(type) ;
		this.value = value ;
	}

	public static PropertyValue create(Object value) {
		Map map = (Map) value ;
		return new PropertyValue(map.get("type"), map.get("value"));
	}
	
	public boolean isBlob() {
		return "Lob".equals(type) ;
	}

	public String type() {
		return type ;
	}
	
	public String asString() {
		return StringUtil.toString(value) ;
	}
	

	public Object asObject() {
		return value;
	}

	public Boolean asBoolean() {
		return Boolean.valueOf(asString());
	}
	
	public long asLong(int dftValue) {
		return longValue(dftValue);
	}

	public long longValue(long dftValue) {
		try {
			return (long) Double.parseDouble(asString());
		} catch (NumberFormatException e) {
			return dftValue;
		}
	}
}
