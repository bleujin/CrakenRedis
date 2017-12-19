package net.bleujin.rcraken;

import java.awt.image.SampleModel;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import net.ion.framework.util.Debug;

public class WriteNodeTest extends TestBaseCrakenRedis {

	@Test
	public void testRefTo() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").refTo("friend", "/emp/hero").merge();
			wsession.pathBy("/emp/hero").property("name", "hero").merge();
			return null;
		}).thenAccept(nill ->{
			ReadNode hero = rsession.pathBy("/emp/bleujin").ref("friend") ;
			hero.debugPrint();
		}) ;
	}

	@Test
	public void testSpeed() throws Exception {
		long start = System.currentTimeMillis() ;
		for (int i = 0; i < 100 ; i++) {
			final int age = i ;
			rsession.tran(wsession -> {
				wsession.pathBy("/emp/bleujin").property("age", age).merge(); ;
				wsession.pathBy("/emp/hero").property("age", 30).merge(); ;
				return null;
			}) ;
		}
		Debug.line(System.currentTimeMillis() - start);
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

}
