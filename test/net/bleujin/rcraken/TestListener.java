package net.bleujin.rcraken;

import net.bleujin.rcraken.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;

public class TestListener extends TestBaseCrakenRedis {

	
	public void testConfirm() throws Exception {
		rsession.workspace().addListener(new NodeListener() {
			@Override
			public void onMerged(EventType etype, Fqn fqn, JsonObject value, JsonObject oldValue) {
				Debug.line(etype, fqn, value, oldValue);
			}
		}) ;
		
		rsession.tran(SAMPLE) ;
	}
}
