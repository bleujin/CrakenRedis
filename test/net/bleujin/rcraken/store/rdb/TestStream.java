package net.bleujin.rcraken.store.rdb;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class TestStream {

	protected static Craken c;
	protected ReadSession rsession;
	
	@BeforeAll
	static void init() throws Exception {
		c = new PGConfig().testBuild() ;
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
	public void testOutput() throws Exception {
		File cat = new File("./resource/6.jpg");
		rsession.tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).property("filename", "cat.jpg").property("file", new FileInputStream(cat)).merge() ;
		}) ;
		
		rsession.pathBy("/emp/bleujin").debugPrint() ;
		
		InputStream input = rsession.pathBy("/emp/bleujin").property("file").asStream() ;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		IOUtil.copyNClose(input, bout) ;
		assertEquals(cat.length(), bout.size());
		
	}
}
