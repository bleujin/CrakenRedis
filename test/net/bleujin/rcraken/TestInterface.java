package net.bleujin.rcraken;

import net.ion.framework.util.Debug;

public class TestInterface extends TestBaseCrakenRedis {


	public void testFirst() throws Exception {
		
		rsession.workspace().flushAll() ;
		long start = System.currentTimeMillis() ;

		
		ReadNode root = rsession.pathBy("/") ;
		assertEquals(true, rsession.exist("/"));
		assertEquals(false, root.hasProperty("not"));
		
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode node = wsession.pathBy("/test");
				node.property("name", "bleujin");
				return null;
			}
		}) ;
		
		
		// this.rsession = c.login("testworkspace") ;
		assertEquals("bleujin", rsession.pathBy("/test").asString("name")) ; 
		Debug.line(System.currentTimeMillis() - start);
	}
	
	
	
	
}
