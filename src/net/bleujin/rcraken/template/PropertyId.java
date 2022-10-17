package net.bleujin.rcraken.template;

import java.util.Map;
import java.util.Map.Entry;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.StringUtil;

public class PropertyId {

	private String key;
	private String type;

	private PropertyId(String key, Object type) {
		this.key = key ;
		this.type = StringUtil.toString(type) ;
	}

	public static PropertyId create(Entry<String, Object> entry) {
		return new PropertyId(entry.getKey(), ((Map)(entry.getValue())).get("type"));
	}
	
	public String idString() {
		return key ;
	}
	
	public String type() {
		return type ;
	}

}
