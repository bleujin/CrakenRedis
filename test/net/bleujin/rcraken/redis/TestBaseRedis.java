package net.bleujin.rcraken.redis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.tbase.TestBaseRCraken;

public class TestBaseRedis {

	
	protected static Craken c;
	protected ReadSession rsession;

	public static WriteJob<Void> SAMPLE = TestBaseRCraken.SAMPLE ;
	
	@BeforeAll
	static void init() throws Exception {
		c = CrakenConfig.redisSingle().build() ;
		c.start() ;
		
	}
	
	@AfterAll
	static void done() throws Exception {
		c.shutdown();
	}


	@BeforeEach
	void setUp(){
		rsession = c.login("testworkspace") ;
		rsession.workspace().removeSelf() ;
	}
	
	
	
	
}
