package net.bleujin.rcraken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import net.ion.framework.util.Debug;

public class ReadNodeTest extends TestBaseCrakenRedis{

	@Test
	public void parentExist() throws Exception {
		rsession.tran(SAMPLE) ;
		assertEquals(true, rsession.exist("/emp"));
		assertEquals(false, rsession.pathBy("/emp").property("name").isExist()) ;
		
		assertEquals(true, rsession.exist("/emp/bleujin")) ;
		assertEquals(true, rsession.pathBy("/emp/bleujin").property("name").isExist()) ;
	}
	
	@Test
	public void parent() throws Exception {
		assertEquals(true, rsession.pathBy("/emp").parent().isRoot()) ;
		assertEquals(rsession.pathBy("/emp").fqn(), rsession.pathBy("/emp/bleujin").parent().fqn()) ;
	}
	
	@Test
	public void root() throws Exception {
		assertEquals("/", rsession.pathBy("/").fqn().absPath()) ;
	}
	

	@Test
	public void childrenNames() throws Exception {
		rsession.tran(SAMPLE) ;
		
		rsession.pathBy("/").childrenNames().stream().forEach(Debug::println);
		rsession.pathBy("/emp").childrenNames().stream().forEach(Debug::println);
	}
	

	@Test
	public void whenNotExist() throws Exception {
		rsession.tran(SAMPLE) ;
		
		rsession.pathBy("/notexist").children().debugPrint();
		assertNull(rsession.pathBy("/notexist").property("name").asString()) ;
	}

}
