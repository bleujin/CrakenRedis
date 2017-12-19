package net.bleujin.rcraken;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class TestBaseCrakenRedis {

	
	protected static Craken c;
	protected ReadSession rsession;

	protected static WriteJob<Void> SAMPLE = new WriteJob<Void>() {
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
		c = CrakenConfig.redisSingle().build() ;
		c.start() ;
		
	}
	
	@AfterAll
	static void done() throws Exception {
		c.shutdownSelf();
	}


	@BeforeEach
	void setUp(){
		rsession = c.login("testworkspace") ;
		rsession.workspace().removeSelf() ;
	}
	
	
	
	
}
