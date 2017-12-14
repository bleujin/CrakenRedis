# CrakenRedis


I wonder if I can have a more simple API(CRUD, Index/Search) when Based on redis,


```java

public class TestInterface extends TestCase {

	private Craken c;
	private ReadSession rsession;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.c = CrakenConfig.redis().singleServer().build().start();

		this.rsession = c.login("testworkspace");
		this.rsession.workspace().destorySelf();
	}

	@Override
	protected void tearDown() throws Exception {
		c.destroySelf();
		super.tearDown();
	}

	public void testFirst() throws Exception {
		long start = System.currentTimeMillis();

		ReadNode root = rsession.pathBy("/");
		assertEquals(true, rsession.exist("/"));
		assertEquals(false, root.hasProperty("not"));

		rsession.tran(wsession -> {
			wsession.pathBy("/test").property("name", "bleujin").property("age", 20).merge();
			return null;
		});

		assertEquals("bleujin", rsession.pathBy("/test").asString("name"));
		assertEquals(20, rsession.pathBy("/test").property("age").asInt());
		Debug.line(System.currentTimeMillis() - start);
	}

}

```