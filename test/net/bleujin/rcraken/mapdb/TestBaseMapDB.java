package net.bleujin.rcraken.mapdb;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.redis.TestBaseRedis;

public class TestBaseMapDB {

	
	protected static Craken c;
	protected ReadSession rsession;
	public static WriteJob<Void> SAMPLE = TestBaseRedis.SAMPLE ;
	
	@BeforeAll
	static void init() throws Exception {
		c = CrakenConfig.mapMemory().build() ;
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

	@AfterEach
	void tearDown() throws IOException {
		rsession.workspace().removeSelf() ;
	}
	
}
