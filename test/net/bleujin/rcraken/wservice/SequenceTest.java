package net.bleujin.rcraken.wservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.redis.TestBaseRedis;
import net.bleujin.rcraken.tbase.TestBaseRCraken;

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
}
