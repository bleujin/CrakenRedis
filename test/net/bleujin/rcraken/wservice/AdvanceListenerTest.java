package net.bleujin.rcraken.wservice;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sun.xml.internal.ws.api.server.WSEndpoint;

import net.bleujin.rcraken.WriteJobNoReturn;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.extend.CDDHandler;
import net.bleujin.rcraken.extend.CDDModifiedEvent;
import net.bleujin.rcraken.extend.CDDRemovedEvent;
import net.bleujin.rcraken.extend.ModifyCDDHandler;
import net.bleujin.rcraken.mapdb.TestBaseMapDB;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.util.Debug;

public class AdvanceListenerTest extends TestBaseRCraken {

	
	@Test
	public void chainWrite() throws Exception { // like trigger.
		rsession.workspace().add(new ModifyCDDHandler("/emp/{userid}") {
			public WriteJobNoReturn modified(Map<String, String> resolveMap, CDDModifiedEvent event) {
				return (wsession -> {
					wsession.pathBy("/dept/dev").refTo("dept", "/emp/" + resolveMap.get("userid")).merge();
				});
			}
		});
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").merge();;
		}) ;
		
		rsession.root().walkBreadth().debugPrint();
	}
}
