# CrakenRedis


I wonder if I can have a more simple API(CRUD, Index/Search) when Based on redis,


This project is a kind of redis client. 
If you want to test, 
you have to install redis first.(http://hwigyeom.ntils.com/entry/Windows-%EC%97%90-Redis-%EC%84%A4%EC%B9%98%ED%95%98%EA%B8%B0-1)

The necessary required libraries are under the /lib directory.(JDK8)

```java

public class TestInterface extends TestCase {

	private Craken c;
	private ReadSession rsession;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.c = CrakenConfig.redis().singleServer().build().start();

		this.rsession = c.login("testworkspace");
		this.rsession.workspace().removeSelf();
	}

	@Override
	protected void tearDown() throws Exception {
		c.destroySelf();
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
		Debug.line(); 
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp").children().stream().limit(2).map(wn -> wn.property("age", wn.property("age").asInt() * 2)).forEach(w -> w.merge());
			return null;
			
		}).thenAccept((r) ->{
			rsession.pathBy("/emp").children().stream().limit(5).filter(rnode -> rnode.property("age").asLong() >= 25L)
				.sorted((n1, n2) -> n2.property("age").asInt() - n1.property("age").asInt())
				.forEach(System.out::println);
		}) ;
	}

}

```