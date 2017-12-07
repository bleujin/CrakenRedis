package net.bleujin.rcraken;

import java.util.concurrent.Future;

import net.ion.framework.util.Debug;

public class TestReadNode extends TestBaseCrakenRedis{

	public void testParentExist() throws Exception {
		rsession.workspace().destorySelf() ;
		
		rsession.tran(SAMPLE) ;
		assertEquals(true, rsession.exist("/emp"));
		assertEquals(false, rsession.pathBy("/emp").property("name").isExist()) ;
		
		assertEquals(true, rsession.exist("/emp/bleujin")) ;
		assertEquals(true, rsession.pathBy("/emp/bleujin").property("name").isExist()) ;
	}
	
	public void testParent() throws Exception {
		assertEquals(true, rsession.pathBy("/emp").parent().isRoot()) ;
		assertEquals(rsession.pathBy("/emp").fqn(), rsession.pathBy("/emp/bleujin").parent().fqn()) ;
	}
	
	public void testRoot() throws Exception {
		assertEquals("/", rsession.pathBy("/").fqn().absPath()) ;
	}
	

	public void testChildrenNames() throws Exception {
		rsession.workspace().destorySelf() ;
		rsession.tran(SAMPLE) ;
		
		Debug.line(rsession.pathBy("/").childrenNames()) ;
		Debug.line(rsession.pathBy("/emp").childrenNames()) ;
	}
	
	
}
