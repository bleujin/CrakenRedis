package net.bleujin.rcraken;

import junit.framework.TestCase;
import net.ion.framework.util.Debug;

public class TestInterface extends TestCase {

	private Craken c;
	private ReadSession rsession;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.c = CrakenConfig.redisSingle().build().start();

		this.rsession = c.login("testworkspace");
		this.rsession.workspace().removeSelf();
	}

	@Override
	protected void tearDown() throws Exception {
		c.shutdownSelf();
		super.tearDown();
	}

	public void testFirst() throws Exception {

		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).merge();
			wsession.pathBy("/emp/hero").property("name", "hero").property("age", 30).merge();
			wsession.pathBy("/emp/hero/address").property("city", "seoul").merge();
			return null;
		});

		assertEquals("bleujin", rsession.pathBy("/emp/bleujin").asString("name"));
		assertEquals(30, rsession.pathBy("/emp/hero").property("age").asInt());
		
		rsession.pathBy("/emp").children().forEach(System.out::println); // print /emp/bleujin, /emp/hero
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp").children().stream().limit(2).map(wn -> wn.property("age", wn.property("age").asInt() * 2)).forEach(w -> w.merge());
			return null;
		});
		
		rsession.pathBy("/emp").children().stream().limit(5).filter(rnode -> rnode.property("age").asLong() >= 25L)
			.sorted((n1, n2) -> n2.property("age").asInt() - n1.property("age").asInt())
			.forEach(System.out::println);
	}

}
