package net.bleujin.rcraken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.util.Debug;

public class ReadNodeTest extends TestBaseRCraken{

	@Test
	public void parentExist() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			assertEquals(true, rsession.exist("/emp"));
			assertEquals(false, rsession.pathBy("/emp").property("name").isExist()) ;
			
			assertEquals(true, rsession.exist("/emp/bleujin")) ;
			assertEquals(true, rsession.pathBy("/emp/bleujin").property("name").isExist()) ;
		}) ;
	}
	
	@Test
	public void parent() throws Exception {
		assertEquals(true, rsession.pathBy("/emp").parent().isRoot()) ;
		assertEquals(rsession.pathBy("/emp").fqn(), rsession.pathBy("/emp/bleujin").parent().fqn()) ;
	}
	
	@Test
	public void root() throws Exception {
		assertEquals("/", rsession.pathBy("/").fqn().absPath()) ;
		assertEquals(true, rsession.pathBy("/").fqn().getParent().isRoot()) ;
		assertEquals("/", rsession.pathBy("/").fqn().getParent().absPath()) ;
	}
	

	@Test
	public void childrenNames() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			rsession.pathBy("/").childrenNames().stream().forEach(Debug::println);
			rsession.pathBy("/emp").childrenNames().stream().forEach(Debug::println);
		}) ;
		
	}
	

	@Test
	public void whenNotExist() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			rsession.pathBy("/notexist").children().stream().skip(0).limit(10).debugPrint();
			assertNull(rsession.pathBy("/notexist").property("name").asString()) ;
		}) ;
		
	}

}
