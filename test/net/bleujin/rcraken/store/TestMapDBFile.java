package net.bleujin.rcraken.store;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteJobNoReturn;
import net.bleujin.rcraken.extend.CDDModifiedEvent;
import net.bleujin.rcraken.extend.CDDRemovedEvent;
import net.bleujin.rcraken.extend.ModifyCDDHandler;
import net.bleujin.rcraken.redis.TestBaseRedis;
import net.ion.framework.util.Debug;
import net.ion.framework.util.RandomUtil;

public class TestMapDBFile {
	protected static Craken c;
	protected ReadSession rsession;
	
	@BeforeAll
	static void init() throws Exception {
		Maker maker = DBMaker.fileDB(new File("./resource/mapdb/map.db")).fileMmapEnableIfSupported() ;
		c = MapConfig.fromMaker(maker).lobRootDir(new File("./resource/lob")).build() ;
		c.start() ;
		
	}
	
	@AfterAll
	static void done() throws Exception {
		c.shutdown();
	}


	@BeforeEach
	void setUp(){
		rsession = c.login("testworkspace") ;
	}
	
	
	@Test
	public void writeData() throws Exception {
		rsession.tran(TestBaseRedis.SAMPLE).get() ;
		rsession.pathBy("/").walkDepth().debugPrint();
	}

	
	@Test
	public void deleteData() throws Exception {
		rsession.tran(TestBaseRedis.SAMPLE).get() ;
		rsession.tranSync(wsession -> {
			wsession.pathBy("/emp").removeSelf(); 
		}) ;
		
		Debug.line(rsession.pathBy("/").childrenNames()) ;
		
		rsession.pathBy("/").children().debugPrint();
	}

	
	@Test
	public void readDataAfterWrite() throws Exception {
		rsession.pathBy("/").walkDepth().debugPrint();
		rsession.workspace().removeSelf() ;
		rsession.pathBy("/").walkDepth().debugPrint();
	}
	
	@Test
	public void testEvent() throws Exception {

		rsession.workspace().add(new ModifyCDDHandler("/emp/{userid}") {
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
