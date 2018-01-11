package net.bleujin.rcraken.wservice;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sun.xml.internal.ws.api.server.WSEndpoint;

import net.bleujin.rcraken.WriteJobNoReturn;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.extend.CDDHandler;
import net.bleujin.rcraken.extend.CDDModifiedEvent;
import net.bleujin.rcraken.extend.CDDRemovedEvent;
import net.bleujin.rcraken.tbase.TestBaseMapDB;
import net.ion.framework.util.Debug;

public class AdvanceListenerTest extends TestBaseMapDB {

	
	@Test
	public void chainWrite() throws Exception { // like trigger.
		rsession.workspace().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/emp/{userid}";
			}
			
			@Override
			public WriteJobNoReturn modified(Map<String, String> resolveMap, CDDModifiedEvent event) {
				return new WriteJobNoReturn() {
					@Override
					public void handle(WriteSession wsession) throws Exception {
						wsession.pathBy("/dept/dev").refTo("dept", "/emp/" + resolveMap.get("userid")).merge();
					}
				};
			}
			
			@Override
			public String id() {
				return "cdd.chain";
			}
			
			@Override
			public WriteJobNoReturn deleted(Map<String, String> resolveMap, CDDRemovedEvent event) {
				return null;
			}
		});
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").merge();;
		}) ;
		
		rsession.root().walkBreadth().debugPrint();
	}
}
