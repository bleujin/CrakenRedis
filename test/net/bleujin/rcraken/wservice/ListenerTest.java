package net.bleujin.rcraken.wservice;

import java.util.Map;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.WriteJobNoReturn;
import net.bleujin.rcraken.extend.CDDHandler;
import net.bleujin.rcraken.extend.CDDModifiedEvent;
import net.bleujin.rcraken.extend.CDDRemovedEvent;
import net.bleujin.rcraken.extend.NodeListener;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;

public class ListenerTest extends TestBaseRCraken {

	@Test
	public void fireWhenMerge() throws Exception {
		rsession.workspace().removeSelf() ;
		rsession.workspace().addListener(new NodeListener() {
			public void onChanged(EventType etype, Fqn fqn, JsonObject value, JsonObject oldValue) {
				Debug.line(etype, fqn, value, oldValue);
			}
			@Override
			public String id() {
				return "test";
			}
		}) ;
		
		rsession.tran(SAMPLE) ;
		rsession.tran( wsession -> {
			wsession.pathBy("/emp/bleujin").removeSelf();
			return null ;
		}) ;
		
		rsession.workspace().removeListener("test") ;
		rsession.tran( wsession -> {
			wsession.pathBy("/emp/hero").removeSelf();
			return null ;
		}) ;
	}
	
	@Test
	public void fireCDDHandler() throws Exception {
		rsession.workspace().removeSelf() ;
		rsession.workspace().add(new CDDHandler() {
			public String pathPattern() {
				return "/emp/{userid}";
			}
			public WriteJobNoReturn modified(Map<String, String> resolveMap, CDDModifiedEvent event) {
				Debug.line("modified", resolveMap, event.newProperty("name").asString(), event.oldProperty("name").asString());
				return null;
			}
			public String id() {
				return "cdd.test";
			}
			public WriteJobNoReturn deleted(Map<String, String> resolveMap, CDDRemovedEvent event) {
				Debug.line("removed", resolveMap, event.oldProperty("name"));
				return null;
			}
		});

		rsession.tran(SAMPLE) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/hero").removeSelf() ;	
		}) ;
		rsession.tran(SAMPLE) ;
	}
}
