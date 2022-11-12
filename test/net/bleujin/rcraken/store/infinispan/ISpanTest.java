package net.bleujin.rcraken.store.infinispan;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.redis.TestBaseRedis;
import net.ion.framework.util.Debug;

public class ISpanTest {

	protected static Craken c;
	protected ReadSession rsession;
	
	@BeforeAll
	static void init() throws Exception {
		c = ISpanMaker.localMemory().build() ;
		c.start() ;
		
	}
	
	@AfterAll
	static void done() throws Exception {
		c.shutdown();
	}


	@BeforeEach
	void setUp(){
		rsession = c.login("testworkspace") ;
	}
	
	
	@Test
	public void writeData() throws Exception {
		rsession.tran(TestBaseRedis.SAMPLE).get() ;
		rsession.pathBy("/").walkDepth().debugPrint();
	}

	
	@Test
	public void deleteData() throws Exception {
		rsession.tran(TestBaseRedis.SAMPLE).get() ;
		rsession.tranSync(wsession -> {
			wsession.pathBy("/emp").removeSelf(); 
		}) ;
		
		Debug.line(rsession.pathBy("/").childrenNames()) ;
		
		rsession.pathBy("/").children().debugPrint();
	}

	
	@Test
	public void readDataAfterWrite() throws Exception {
		rsession.pathBy("/").walkDepth().debugPrint();
		rsession.workspace().removeSelf() ;
		rsession.pathBy("/").walkDepth().debugPrint();
	}

}
