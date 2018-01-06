package net.bleujin.rcraken;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.tbase.TestBaseCrakenRedis;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class PropertyTest extends TestBaseCrakenRedis {

	@Test
	public void primitiveProperty() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/property").property("string", "bleujin")
				.property("boolean", true)
				.property("calendar", Calendar.getInstance())
				.property("long", Long.MAX_VALUE)
				.property("double", Double.valueOf("3.5"))
				.property("integer", Integer.MAX_VALUE * 1L).merge() ;
			return null ;
		}).thenAccept(nil ->{
			ReadNode found = rsession.pathBy("/property") ;
			assertEquals("bleujin", found.property("string").asString());
			assertEquals(true, found.property("boolean").asBoolean());
			assertEquals(Long.MAX_VALUE, found.property("long").asLong());
			assertEquals(Double.valueOf("3.5"), Double.valueOf(found.property("long").asDouble()));
			assertEquals(Integer.MAX_VALUE, found.property("integer").asLong());
			assertEquals(Calendar.getInstance().get(Calendar.DATE), found.property("calendar").asDate().get(Calendar.DATE));
		}) ;
		
	}
	
	@Test
	public void blobProperty() throws Exception {
		
		FileInputStream fis = new FileInputStream(new File("./resource/helloworld.txt")) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("data", fis).merge() ;
			return null ;
		}).thenAccept(nil -> {
			try {
				rsession.pathBy("/emp/bleujin").properties().forEach(System.out::println);
				InputStream input = rsession.pathBy("/emp/bleujin").property("data").asStream() ;
				Debug.line(IOUtil.toStringWithClose(input));
				rsession.workspace().removeSelf() ; // removed all lob 
//				Debug.line(rsession.workspace().client().getKeys().findKeysByPattern(rsession.workspace().lobPrefix() + "/*")) ;

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}) ;
		
	}
	

	@Test
	public void blobPropertyWhenRemove() throws Exception {
		FileInputStream fis = new FileInputStream(new File("./resource/helloworld.txt")) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("data", fis).merge() ;
			return null ;
		}) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").removeSelf();
			return null ;
		}).thenAccept(nil ->{
//			assertEquals(0, rsession.workspace().client().getKeys().findKeysByPattern(rsession.workspace().lobPrefix() + "*").size()) ;
		}) ;
	}
	
	@Test
	public void arrayProperty() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("city", "seoul", "pusan", "inchen").merge() ;
			return null ;
		}).thenAccept(nil -> {
			assertEquals("bleujin", rsession.pathBy("/emp/bleujin").property("name").asStrings()[0]) ;
			assertEquals("seoul", rsession.pathBy("/emp/bleujin").property("city").asString()) ; 
			assertEquals(3, rsession.pathBy("/emp/bleujin").property("city").asStrings().length) ;
		}) ;
		
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("city", "seoul", "pusan").merge() ;
			return null ;
		}).thenAccept(nil ->{
			assertEquals("bleujin", rsession.pathBy("/emp/bleujin").property("name").asStrings()[0]) ;
			assertEquals("seoul", rsession.pathBy("/emp/bleujin").property("city").asString()) ; 
			assertEquals(2, rsession.pathBy("/emp/bleujin").property("city").asStrings().length) ;
		}) ;
	}
	
	@Test
	public void unSet() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("city", "seoul", "pusan", "inchen").merge() ;
			return null ;
		}) ;

		rsession.tran(wsession -> {
			WriteNode wnode = wsession.pathBy("/emp/bleujin");
			JsonObject jsonvalue = wnode.property("name", "bleujin").unset("city");
			assertEquals("seoul", jsonvalue.asString("value"));
			assertEquals("pusan", jsonvalue.asJsonArray("values").get(0).getAsString());
			assertEquals("inchen", jsonvalue.asJsonArray("values").get(1).getAsString());
			wnode.merge(); // alert
			return null ;
		}).thenAccept(nil ->{
			assertEquals(false, rsession.pathBy("/emp/bleujin").hasProperty("city")) ;
		}) ;
		// when lob
	}
	

	@Test
	public void reference() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").merge() ;
			wsession.pathBy("/emp/hero").property("name", "bleujin").merge() ;
			wsession.pathBy("/emp/jin").property("name", "bleujin").merge() ;
			wsession.pathBy("/dept/dev").refTo("account", "/emp/bleujin", "/emp/hero", "/emp/jin", "/notfound").merge() ;
			return null ;
		}).thenAccept(nil -> {
			rsession.pathBy("/dept/dev").ref("account").debugPrint(); 
			rsession.pathBy("/dept/dev").refs("account").debugPrint();
		}) ;
		
	}

	@Test
	public void encrypt() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("id", "bleujin").encrypt("pwd", "1234").merge();
			return null;
		}).thenAccept(nil ->{
			ReadNode found = rsession.pathBy("/emp/bleujin");
			Debug.line(found.property("pwd").asString()) ;
			assertEquals(true, found.isMatch("pwd", "1234")) ;
		}) ;

	}

}
