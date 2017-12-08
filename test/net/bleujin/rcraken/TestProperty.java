package net.bleujin.rcraken;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class TestProperty extends TestBaseCrakenRedis{

	public void testBlob() throws Exception {
		
		FileInputStream fis = new FileInputStream(new File("./resource/helloworld.txt")) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("data", fis).merge() ;
			return null ;
		}) ;
		
		rsession.pathBy("/emp/bleujin").properties().forEach(System.out::println);
		
		InputStream input = rsession.pathBy("/emp/bleujin").property("data").asStream() ;
		Debug.line(IOUtil.toStringWithClose(input));

		rsession.workspace().destorySelf() ; // removed all lob 
		Debug.line(rsession.workspace().client().getKeys().findKeysByPattern(rsession.workspace().lobPrefix() + "/*")) ;
	}
	

	
	public void testBlobWhenRemove() throws Exception {
		FileInputStream fis = new FileInputStream(new File("./resource/helloworld.txt")) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("data", fis).merge() ;
			return null ;
		}) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").removeSelf();
			return null ;
		}) ;
		
		assertEquals(0, rsession.workspace().client().getKeys().findKeysByPattern(rsession.workspace().lobPrefix() + "*").size()) ;
		
	}
	
	public void testArray() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("city", "seoul", "pusan", "inchen").merge() ;
			return null ;
		}) ;
		
		assertEquals("bleujin", rsession.pathBy("/emp/bleujin").property("name").asStrings()[0]) ;
		assertEquals("seoul", rsession.pathBy("/emp/bleujin").property("city").asString()) ; 
		assertEquals(3, rsession.pathBy("/emp/bleujin").property("city").asStrings().length) ;
		
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("city", "seoul", "pusan").merge() ;
			return null ;
		}) ;
		assertEquals("bleujin", rsession.pathBy("/emp/bleujin").property("name").asStrings()[0]) ;
		assertEquals("seoul", rsession.pathBy("/emp/bleujin").property("city").asString()) ; 
		assertEquals(2, rsession.pathBy("/emp/bleujin").property("city").asStrings().length) ;
	}
	
	public void testUnSet() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("city", "seoul", "pusan", "inchen").merge() ;
			return null ;
		}) ;

		rsession.tran(wsession -> {
			JsonObject jsonvalue = wsession.pathBy("/emp/bleujin").property("name", "bleujin").unset("city") ;
			assertEquals("seoul", jsonvalue.asString("value"));
			assertEquals("pusan", jsonvalue.asJsonArray("values").get(0).getAsString());
			assertEquals("inchen", jsonvalue.asJsonArray("values").get(1).getAsString());
			return null ;
		}) ;

		// when lob
	}
	

	
	public void testReference() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").merge() ;
			wsession.pathBy("/emp/hero").property("name", "bleujin").merge() ;
			wsession.pathBy("/emp/jin").property("name", "bleujin").merge() ;
			wsession.pathBy("/dept/dev").refTo("account", "/emp/bleujin", "/emp/hero", "/emp/jin", "/notfound").merge() ;
			return null ;
		}) ;
		
		rsession.pathBy("/dept/dev").ref("account").debugPrint(); 
		rsession.pathBy("/dept/dev").refs("account").debugPrint();
	}

	

}
