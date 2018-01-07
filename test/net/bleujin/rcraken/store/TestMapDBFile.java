package net.bleujin.rcraken.store;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.tbase.TestBaseCrakenRedis;

public class TestMapDBFile {
	protected static Craken c;
	protected ReadSession rsession;
	
	@BeforeAll
	static void init() throws Exception {
		Maker maker = DBMaker.fileDB(new File("./resource/mapdb/map.db")).fileMmapEnableIfSupported() ;
		c = MapConfig.fromMaker(maker).build() ;
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
		rsession.tran(TestBaseCrakenRedis.SAMPLE).get() ;
		rsession.pathBy("/").walkDepth().debugPrint();
	}
	
	
	@Test
	public void readDataAfterWrite() throws Exception {
		rsession.pathBy("/").walkDepth().debugPrint();
		rsession.workspace().removeSelf() ;
		rsession.pathBy("/").walkDepth().debugPrint();
	}
	
}
