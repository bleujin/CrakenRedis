package net.bleujin.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.tbase.TestBaseRCraken;

public class NodeMoveCopy extends TestBaseRCraken {
	

	@Test
	public void testCopy() throws Exception {
		rsession.tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).property("address", "seoul").merge() ;
			wsession.pathBy("/emp/jin").property("name", "jin").property("age", 25).merge() ;
			wsession.pathBy("/emp/hero/address").property("name", "hero").property("age", 30).merge() ;
			
			
			wsession.pathBy("/emp").copySelf("/emp2") ;
		}).thenAccept(nil ->{
			assertEquals(true, rsession.exist("/emp2"));
			assertEquals(4, rsession.pathBy("/emp").walkBreadth().size()); 
			assertEquals(4, rsession.pathBy("/emp2").walkBreadth().size()); 
			assertEquals(true, rsession.exist("/emp2/hero/address"));
			assertEquals("hero", rsession.pathBy("/emp2/hero/address").asString("name"));
		}) ;
	}

	@Test
	public void testMove() throws Exception {
		rsession.tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).property("address", "seoul").merge() ;
			wsession.pathBy("/emp/jin").property("name", "jin").property("age", 25).merge() ;
			wsession.pathBy("/emp/hero/address").property("name", "hero").property("age", 30).merge() ;
			
			wsession.pathBy("/emp").moveSelf("/emp2") ;
		}).thenAccept(nil ->{
			assertEquals(false, rsession.exist("/emp"));
			assertEquals(4, rsession.pathBy("/emp2").walkBreadth().size()); 
			assertEquals(true, rsession.exist("/emp2/hero/address"));
			assertEquals("hero", rsession.pathBy("/emp2/hero/address").asString("name"));
		}) ;
	}
	

}
