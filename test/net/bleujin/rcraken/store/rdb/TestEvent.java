package net.bleujin.rcraken.store.rdb;

import java.util.Map;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.WriteJobNoReturn;
import net.bleujin.rcraken.extend.CDDHandler;
import net.bleujin.rcraken.extend.CDDModifiedEvent;
import net.bleujin.rcraken.extend.CDDRemovedEvent;
import net.ion.framework.util.Debug;
import net.ion.framework.util.RandomUtil;

public class TestEvent extends TestStdMethod {

	@Test
	public void testEvent() throws Exception {

		rsession.workspace().add(new CDDHandler() {
			@Override
			public String id() {
				return "my.handler";
			}

			@Override
			public String pathPattern() {
				return "/emp/{userid}";
			}

			@Override
			public WriteJobNoReturn modified(Map<String, String> resolveMap, CDDModifiedEvent event) {
				Debug.debug("mod", event.oldProperty("rnd"), event.newProperty("rnd"));
				return null;
			}

			@Override
			public WriteJobNoReturn deleted(Map<String, String> resolveMap, CDDRemovedEvent event) {
				Debug.debug("deleted", event.oldProperty("rnd"));
				return null;
			}
		});
		
		rsession.tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 31).property("rnd", RandomUtil.nextRandomString(10)).merge() ;
			wsession.pathBy("/emp/bleujin").removeSelf() ;
		}) ;
		
		Thread.sleep(1000) ;
	}

	
	@Test
	public void testEventWhenCached() throws Exception {
		c = ((PGCraken)c).cached(1000) ;
		rsession = c.login("testworkspace") ;
		
		rsession.workspace().add(new CDDHandler() {
			@Override
			public String id() {
				return "my.handler";
			}

			@Override
			public String pathPattern() {
				return "/emp/{userid}";
			}

			@Override
			public WriteJobNoReturn modified(Map<String, String> resolveMap, CDDModifiedEvent event) {
				Debug.debug("mod", event.oldProperty("rnd"), event.newProperty("rnd"));
				return null;
			}

			@Override
			public WriteJobNoReturn deleted(Map<String, String> resolveMap, CDDRemovedEvent event) {
				Debug.debug("deleted", event.oldProperty("rnd"));
				return null;
			}
		});
		
		rsession.tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 31).property("rnd", RandomUtil.nextRandomString(10)).merge() ;
			wsession.pathBy("/emp/bleujin").removeSelf() ;
		}) ;
		
		Thread.sleep(1000) ;
	}

	
	
	
}
