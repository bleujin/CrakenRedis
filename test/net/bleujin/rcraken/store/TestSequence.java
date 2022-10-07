package net.bleujin.rcraken.store;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.extend.Sequence;
import net.ion.framework.util.Debug;

public class TestSequence extends TestMapDBFile {

	
	@Test
	public void createSeq() throws Exception {
		Sequence seq = rsession.workspace().sequence("seq") ;
		seq.delete();
		assertEquals(0, seq.get()) ;
		assertEquals(1, seq.incrementAndGet()) ;
		assertEquals(2, seq.incrementAndGet()) ;
		assertEquals(4, seq.addAndGet(2)) ;
		
		seq.delete();
		assertEquals(0, seq.get()) ;
	}
	
	@Test
	public void testSingleton() throws Exception {
		Sequence seq1 = rsession.workspace().sequence("seq") ;
		Sequence seq2 = rsession.workspace().sequence("seq") ;

		Debug.debug(seq1.get()) ;
		long val = seq1.incrementAndGet() ;

		assertEquals(val, seq2.get()) ;
		
		assertTrue(seq1 == seq2 );
	}

	@Test
	public void testCurrent() throws Exception {
		Sequence seq = rsession.workspace().sequence("seq") ;

		Debug.line(seq.get()) ;
		Debug.line(seq.incrementAndGet()) ;
		Debug.line(c.db().getStore()) ;
	}
}
