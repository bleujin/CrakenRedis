package net.bleujin.rcraken.store.rdb;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteJobNoReturn;
import net.bleujin.rcraken.WriteNode;
import net.bleujin.rcraken.extend.CDDHandler;
import net.bleujin.rcraken.extend.CDDModifiedEvent;
import net.bleujin.rcraken.extend.CDDRemovedEvent;
import net.bleujin.rcraken.extend.ModifyCDDHandler;
import net.ion.framework.db.DBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.session.Session;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.RandomUtil;

public class TestStream extends TestStdMethod{

	
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
