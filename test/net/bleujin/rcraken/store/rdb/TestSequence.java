package net.bleujin.rcraken.store.rdb;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.extend.Sequence;
import net.ion.framework.util.Debug;

public class TestSequence extends TestStdMethod{

	@Test
	public void testSequence() throws Exception {
		Sequence seq = rsession.workspace().sequence("myseq") ;
		Debug.line(seq.incrementAndGet()) ;
		
		seq.set(50) ;
		Debug.line(seq.getAndIncrement()) ;
	}
	
	@Test
	public void testSingleton() throws Exception {
		Sequence seq1 = rsession.workspace().sequence("myseq") ;
		Sequence seq2 = rsession.workspace().sequence("myseq") ;
		
		assertTrue(seq1 == seq2);
	}
}
