package net.bleujin.rcraken.wservice;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.util.Debug;

public class SequenceTest extends TestBaseRCraken {

	@Test
	public void createSeq() throws Exception {
		Sequence seq = rsession.workspace().sequence("seq") ;
		assertEquals(0, seq.get()) ;
		assertEquals(1, seq.incrementAndGet()) ;
		assertEquals(2, seq.incrementAndGet()) ;
		assertEquals(4, seq.addAndGet(2)) ;
		
		seq.delete();
		seq = rsession.workspace().sequence("seq") ;
		assertEquals(0, seq.get()) ;
	}
	
	@Test
	public void testSingleton() throws Exception {
		Sequence seq1 = rsession.workspace().sequence("seq") ;
		Sequence seq2 = rsession.workspace().sequence("seq") ;

		assertEquals(0, seq1.get()) ;
		assertEquals(1, seq1.incrementAndGet()) ;

		assertEquals(1, seq2.get()) ;
		
		assertTrue(seq1 == seq2 );
	}

	@Test
	public void testCurrent() throws Exception {
		Sequence seq = rsession.workspace().sequence("seq") ;

		Debug.line(seq.get()) ;
	}

	
}
