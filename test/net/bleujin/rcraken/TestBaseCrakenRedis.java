package net.bleujin.rcraken;

import junit.framework.TestCase;

public class TestBaseCrakenRedis extends TestCase {

	
	protected Craken c;
	protected ReadSession rsession;

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
