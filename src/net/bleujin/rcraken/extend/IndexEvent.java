package net.bleujin.rcraken.extend;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;

public class IndexEvent {

	private EventType etype;
	private Fqn fqn;
	private JsonObject jvalue;

	IndexEvent(EventType etype, Fqn fqn, JsonObject jvalue) {
		this.etype = etype ;
		this.fqn = fqn ;
		this.jvalue = jvalue ;
	}

	public static IndexEvent create(EventType etype, Fqn fqn, JsonObject jvalue) {
		return new IndexEvent(etype, fqn, jvalue);
	}

	public EventType eventType() {
		return etype ;
	}
	
	public Fqn fqn() {
		return fqn ;
	}
	
	public JsonObject jsonValue() {
		return jvalue ;
	}
	
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this) ;
	}
}
