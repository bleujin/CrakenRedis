package net.bleujin.rcraken.extend;

import net.bleujin.rcraken.Fqn;
import net.ion.framework.parse.gson.JsonObject;

public interface NodeListener {

	public enum EventType {
		CREATED, UPDATED, REMOVED
	}

	public String id()  ;
	public void onMerged(EventType etype, Fqn fqn, JsonObject value, JsonObject oldValue);

}
