package net.bleujin.rcraken.store;

import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.nsearcher.config.CentralConfig;

public class TestMapDB extends TestBaseMapDB{

	
	@Test
	public void initialize() throws Exception{
		rsession.tran(SAMPLE).get() ;
		rsession.pathBy("/").walkDepth().debugPrint(); 
	}
	
	@Test
	public void lobProperty() throws Exception {
		rsession.tran(wsession -> {
			FileInputStream fis = new FileInputStream("./resource/helloworld.txt") ;
			wsession.pathBy("/bleujin").property("helloworld", fis).merge(); ;
			return null;
		}) ;
		
		InputStream input = rsession.pathBy("/bleujin").property("helloworld").asStream() ;
		Debug.line(IOUtil.toStringWithClose(input));
	}
	
	@Test
	public void indexData() throws Exception {
		rsession.workspace().indexCntral(CentralConfig.newRam().build()) ;
		rsession.tran(SAMPLE).get() ;
		
		rsession.pathBy("/").childQuery("", true).find().debugPrint();
	}
	
}
