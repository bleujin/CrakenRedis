# CrakenRedis


Craken을 사용하다보니 좀더 단순한 API만 있어도 되지 않을까 해서 redis 기반으로 좀더 심플하게 만들어 보고 있음. 



```java

  public class TestInterface extends TestCase {

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
```