package net.bleujin.rcraken.extend;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.bleujin.rcraken.extend.NodeListener.EventType;
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
