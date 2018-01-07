package net.bleujin.rcraken;


import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import net.ion.framework.util.Debug;

public class ReadMe {

	@Test
	public void withRedis() throws Exception {
		Craken c = CrakenConfig.redisSingle().build().start();
		stdTest(c); 
	}
	
	@Test
	public void withMapDB() throws Exception {
		Craken c = CrakenConfig.mapMemory().build().start();
		stdTest(c); 
	}
	
	
	
	void stdTest(Craken c) throws InterruptedException, ExecutionException {
		ReadSession rsession = c.login("testworkspace");
		rsession.workspace().removeSelf(); // clear workspace

		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).merge();
			wsession.pathBy("/emp/hero").property("name", "hero").property("age", 30).merge();
			wsession.pathBy("/emp/hero/address").property("city", "seoul").merge();
			return null;
		});

		assertEquals("bleujin", rsession.pathBy("/emp/bleujin").asString("name"));
		assertEquals(30, rsession.pathBy("/emp/hero").property("age").asInt());
		
		rsession.pathBy("/emp").children().forEach(System.out::println); // print /emp/bleujin, /emp/hero
		Debug.line(); 
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp").children().stream().limit(2).map(wn -> wn.property("age", wn.property("age").asInt() * 2)).forEach(w -> w.merge());
			return null;
			
		}).get() ;

		rsession.pathBy("/emp").children().stream().limit(5).filter(rnode -> rnode.property("age").asLong() >= 25L)
			.sorted((n1, n2) -> n2.property("age").asInt() - n1.property("age").asInt())
			.forEach(System.out::println);
		
		c.shutdown();
	}

}
