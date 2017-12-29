package net.bleujin.rcraken.store;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.mte.Engine;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.nsearcher.config.CentralConfig;

public class TestMapDB extends TestBaseMapDB{

	
	@Test
	public void initialize() throws Exception{
		rsession.tran(TestBaseCrakenRedis.SAMPLE).get() ;
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
		rsession.tran(TestBaseCrakenRedis.SAMPLE).get() ;
		
		rsession.pathBy("/").childQuery("", true).find().debugPrint();
	}

	
	@Test 
	public void indexDataWhenNotSet() throws Exception {
		assertThrows(IllegalStateException.class, () ->{
			rsession.tran(TestBaseCrakenRedis.SAMPLE).get() ;
			rsession.pathBy("/").childQuery("", true).find().debugPrint();
		}) ;
	}
	
	@Test
	public void directTransform() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			ReadNode self = rsession.pathBy("/emp");
			//self.children().stream().where("age", 10).debugPrint();
			
			Engine engine = rsession.workspace().parseEngine();
			Debug.line(engine.transform("${foreach self.children().stream().where(\"this.age>20 and this.name='jin'\").toList() child \n}${child}${end}", MapUtil.<String, Object>create("self", self))) ;
		}) ;
	} // i need template engine with lambda expression

	
}
