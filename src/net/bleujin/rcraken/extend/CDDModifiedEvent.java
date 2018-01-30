package net.bleujin.rcraken.extend;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.Property;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.ObjectUtil;

public class CDDModifiedEvent {
	private Fqn key;
	private Map<String, Property> newProperties;
	private EventType etype = EventType.UPDATED ;
	private Map<String, Property> oldProperties;
	
	public CDDModifiedEvent(Fqn dataKey, Map<String, Property> newProperties, Map<String, Property> oldProperties) {
		this.key = dataKey ;
		this.newProperties = newProperties ;
		this.oldProperties = oldProperties ;
	}

	public final static CDDModifiedEvent create(ReadSession rsession, Fqn dataKey, JsonObject newData, JsonObject oldData){
		return new CDDModifiedEvent(dataKey, newData.keySet().stream().collect(Collectors.toMap(k -> k, k ->  Property.create(rsession, dataKey, k, newData.asJsonObject(k)))), 
				oldData.keySet().stream().collect(Collectors.toMap(k -> k, k ->  Property.create(rsession, dataKey, k, oldData.asJsonObject(k)))))  ;
	}
	
	public Fqn getKey(){
		return key ;
	}
	
	public Stream<Property> newValues(){
		return newProperties.values().stream() ;
	}
	
	public Property newProperty(String propId){
		return ObjectUtil.coalesce(newProperties.get(propId), Property.NOTFOUND) ;
	}

	public Property oldProperty(String propId) {
		return ObjectUtil.coalesce(oldProperties.get(propId), Property.NOTFOUND) ;
	}


	public CDDModifiedEvent etype(EventType etype) {
		this.etype = etype ;
		return this;
	}
	
	public EventType etype(){
		return etype ;
	}

}
