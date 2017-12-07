package net.bleujin.rcraken;

import net.ion.framework.util.Debug;

public class TestInterface extends TestBaseCrakenRedis {


	public void testFirst() throws Exception {
		
		rsession.workspace().destorySelf() ;
		long start = System.currentTimeMillis() ;

		
		ReadNode root = rsession.pathBy("/") ;
		assertEquals(true, rsession.exist("/"));
		assertEquals(false, root.hasProperty("not"));
		
		rsession.tran(wsession ->{
			wsession.pathBy("/test").property("name", "bleujin").merge();
			return null;
		}) ;
		
		
		// this.rsession = c.login("testworkspace") ;
		assertEquals("bleujin", rsession.pathBy("/test").asString("name")) ; 
		Debug.line(System.currentTimeMillis() - start);
	}
	
	
	
	
}
