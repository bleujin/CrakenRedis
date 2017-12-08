package net.bleujin.rcraken.extend;

import net.bleujin.rcraken.TestBaseCrakenRedis;

public class TestSequence extends TestBaseCrakenRedis {

	
	public void testCreateSeq() throws Exception {
		Sequence seq = rsession.workspace().sequence("seq") ;
		assertEquals(0, seq.get()) ;
		assertEquals(1, seq.incrementAndGet()) ;
		assertEquals(2, seq.incrementAndGet()) ;
		assertEquals(4, seq.addAndGet(2)) ;
		
		seq.delete();
		seq = rsession.workspace().sequence("seq") ;
		assertEquals(0, seq.get()) ;
	}
}
