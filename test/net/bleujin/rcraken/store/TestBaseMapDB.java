package net.bleujin.rcraken.store;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;

public class TestBaseMapDB {

	
	protected static Craken c;
	protected ReadSession rsession;
	
	@BeforeAll
	static void init() throws Exception {
		c = CrakenConfig.mapMemory().build() ;
		c.start() ;
		
	}
	
	@AfterAll
	static void done() throws Exception {
		c.shutdownSelf();
	}


	@BeforeEach
	void setUp(){
		rsession = c.login("testworkspace") ;
		rsession.workspace().removeSelf() ;
	}
	
}