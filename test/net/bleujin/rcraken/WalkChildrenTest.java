package net.bleujin.rcraken;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.tbase.TestBaseRCraken;

public class WalkChildrenTest extends TestBaseRCraken{

	@Test
	public void testWalkChildren() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").merge();
			wsession.pathBy("/emp/hero").property("name", "hero").merge();
			wsession.pathBy("/emp/jin").property("name", "jin").merge();
			wsession.pathBy("/emp/bleujin/address").property("city", "seoul").property("date", Calendar.getInstance()).merge();
			return null;
		}).thenAccept(nil -> {
			rsession.pathBy("/emp").walkBreadth().debugPrint();
			rsession.pathBy("/emp").walkDepth().stream().forEach(System.out::println);
		}) ;
	}

	@Test
	public void testWalkRefChildren() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").refTo("friend", "/emp/hero").merge();
			wsession.pathBy("/emp/hero").property("name", "hero").refTo("friend", "/emp/jin").merge();
			wsession.pathBy("/emp/jin").property("name", "jin").refTo("friend", "/emp/bleujin", "/emp/notfound").merge(); // loop
			return null;
		}).thenAccept(nil -> {
			rsession.pathBy("/emp/bleujin").refChildren("friend", 5).stream().forEach(System.out::println);
		}) ;
	}


}
