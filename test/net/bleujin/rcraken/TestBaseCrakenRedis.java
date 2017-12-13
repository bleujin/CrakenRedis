package net.bleujin.rcraken;

import junit.framework.TestCase;

public class TestBaseCrakenRedis extends TestCase {

	
	protected Craken c;
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
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.c = CrakenConfig.redisSingle().build() ;
		c.start() ;
		
		this.rsession = c.login("testworkspace") ;
		this.rsession.workspace().removeSelf() ;
	}
	
	@Override
	protected void tearDown() throws Exception {
		c.destroySelf();
		
		super.tearDown();
	}
	
}
