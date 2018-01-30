package net.bleujin.rcraken.tbase;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;

public class TestBaseRCraken {
	
	protected static Craken c;
	protected ReadSession rsession;
	public static WriteJob<Void> SAMPLE = new WriteJob<Void>() {
		@Override
		public Void handle(WriteSession wsession) throws Exception {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).property("address", "seoul").merge() ;
			wsession.pathBy("/emp/jin").property("name", "jin").property("age", 25).merge() ;
			wsession.pathBy("/emp/hero").property("name", "hero").property("age", 30).merge() ;
			return null;
		}
	};
	
	@BeforeAll
	static void init() throws Exception {
//		c = CrakenConfig.mapMemory().build() ;
//		c = CrakenConfig.redisSingle().build() ;
		c = CrakenConfig.mapFile(new File("./resource/map.db")).build() ;
				
		
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

	@AfterEach
	void tearDown() {
		rsession.workspace().removeSelf() ;
	}
	
}
