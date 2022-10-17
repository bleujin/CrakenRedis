package net.bleujin.rcraken;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;

public class WriteNodeTest extends TestBaseRCraken {

	@Test
	public void testRefTo() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").refTo("friend", "/emp/hero", "/emp/bleujin").merge();
			wsession.pathBy("/emp/hero").property("name", "hero").merge();
			return null;
		}).thenAccept(nill ->{
			ReadNode hero = rsession.pathBy("/emp/bleujin").ref("friend") ;
			hero.debugPrint();
			
			rsession.pathBy("/emp/bleujin").hasRef("friend") ;
		}) ;
	}

	
	@Test
	public void testReadFrom() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").refTo("friend", "/emp/hero", "/emp/bleujin").merge();
			wsession.pathBy("/emp/hero").property("name", "hero").merge();
			return null;
		}) ;
		
		JsonObject jsonNode = rsession.pathBy("/emp/bleujin").toJson() ;
		jsonNode.asJsonObject("property").asJsonObject("name").put("value", "bleujin2") ;
		
		
		Debug.line(jsonNode.toString()) ;
		
		rsession.tran(wsessiono ->{
			wsessiono.readFrom(jsonNode).merge() ;
		}) ;
		
		rsession.pathBy("/emp/bleujin").debugPrint() ;
	}
	
	
	@Test
	public void testSpeed() throws Exception {
		for (int i = 0; i < 5 ; i++) {
			long start = System.currentTimeMillis() ;
			final int age = i ;
			rsession.tran(wsession -> {
				wsession.pathBy("/emp/bleujin").property("age", age).merge(); ;
				wsession.pathBy("/emp/hero").property("age", 30).merge(); ;
				return null;
			}) ;
			Debug.line(System.currentTimeMillis() - start);
		}
		rsession.pathBy("/").walkDepth().debugPrint();
	}
	
	@Test
	public void testOverwrite() throws Exception {
		long start = System.currentTimeMillis() ;
		rsession.tran(SAMPLE) ;
		Debug.line(System.currentTimeMillis() - start);
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("age", 40).merge(); ;
			wsession.pathBy("/emp/hero").property("age", 30).merge(); ;
			return null;
		}) ;

		Debug.line(System.currentTimeMillis() - start);
		rsession.pathBy("/emp/bleujin").debugPrint();
		rsession.pathBy("/emp/hero").debugPrint();
	}
	
	
	@Test
	public void testDelete() throws Exception {
		rsession.tran(wsession -> {
			for (int i = 0; i < 10; i++) {
				wsession.pathBy("/emp/num/" + i).property("num", i).merge();
			}
			wsession.pathBy("/emp").property("name", "dd") ;
			wsession.pathBy("/bleujin").property("num", 0).merge();
			return null ;
		}) ;
		
		rsession.tran(wsesion ->{
			wsesion.pathBy("/emp").removeSelf() ;
			return null ;
		}) ;
		
		
		rsession.pathBy("/").children().debugPrint(); 
	}
	
	
	@Test
	public void testAppend() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/appendtest").append("name", "bleujin").merge() ;
			wsession.pathBy("/appendtest").append("name", "jin", "hero").merge() ;
		}) ;
		
		assertEquals(3, rsession.pathBy("/appendtest").property("name").asSet().size()) ;
	}
	

}
