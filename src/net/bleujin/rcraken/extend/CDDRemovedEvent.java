package net.bleujin.rcraken.extend;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.Property;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ObjectUtil;

public class CDDRemovedEvent {

	private Fqn key;
	private Map<String, Property> oldProperties;
	private EventType etype = EventType.UPDATED ;
	
	public CDDRemovedEvent(Fqn dataKey, Map<String, Property> oldProperties) {
		this.key = dataKey ;
		this.oldProperties = oldProperties ;
	}

	public final static CDDRemovedEvent create(ReadSession rsession, Fqn dataKey, JsonObject oldData){
		return new CDDRemovedEvent(dataKey, oldData.keySet().stream().collect(Collectors.toMap(k -> k, k ->  Property.create(rsession, dataKey, k, oldData.asJsonObject(k)))))  ;
	}
	
	public Fqn getKey(){
		return key ;
	}
	
	public Stream<Property> oldValues(){
		return oldProperties.values().stream() ;
	}
	
	public Property oldProperty(String propId){
		return ObjectUtil.coalesce(oldProperties.get(propId), Property.NOTFOUND) ;
	}


	public CDDRemovedEvent etype(EventType etype) {
		this.etype = etype ;
		return this;
	}
	
	public EventType etype(){
		return etype ;
	}

}
