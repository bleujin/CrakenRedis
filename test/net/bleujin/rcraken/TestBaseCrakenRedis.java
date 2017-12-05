package net.bleujin.rcraken;

import junit.framework.TestCase;

public class TestBaseCrakenRedis extends TestCase {

	
	protected Craken c;
	protected ReadSession rsession;

	protected static TransactionJob<Void> SAMPLE = new TransactionJob<Void>() {
		
		@Override
		public Void handle(WriteSession wsession) throws Exception {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).property("address", "seoul") ;
			wsession.pathBy("/emp/jin").property("name", "jin").property("age", 25) ;
			wsession.pathBy("/emp/hero").property("name", "hero").property("age", 20) ;
			return null;
		}
	};
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.c = CrakenConfig.redis().singleServer().build() ;
		c.start() ;
		
		this.rsession = c.login("testworkspace") ;
	}
	
	@Override
	protected void tearDown() throws Exception {
		c.destroySelf();
		super.tearDown();
	}
	
}
