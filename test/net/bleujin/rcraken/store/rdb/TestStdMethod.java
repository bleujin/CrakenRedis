package net.bleujin.rcraken.store.rdb;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.store.MapConfig;
import net.ion.framework.util.Debug;

public class TestStdMethod {

	protected static Craken c;
	protected ReadSession rsession;
	
	@BeforeAll
	static void init() throws Exception {
		c = new PGConfig().testBuild() ;
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
	public void testStdWrite() throws Exception {
		rsession.tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).merge() ;
			wsession.pathBy("/emp/hero").property("name", "hero").property("age", 30).merge() ;
			wsession.pathBy("/emp/jin").property("name", "jin").property("age", 40).merge() ;
		}) ;
		
	}
	
	@Test
	public void testStdRead() throws Exception {
		//rsession.pathBy("/").children().debugPrint();
		
		Debug.line(rsession.exist("/emp"), rsession.exist("/demp")) ;
	}
}