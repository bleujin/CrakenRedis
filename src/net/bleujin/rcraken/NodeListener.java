package net.bleujin.rcraken;

import net.ion.framework.parse.gson.JsonObject;

public interface NodeListener {

	public enum EventType{
		CREATED, UPDATED, REMOVED
	}

	public void onMerged(EventType removed, Fqn fqn, JsonObject value, JsonObject oldValue) ;

}
