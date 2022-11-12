package net.bleujin.rcraken.store.rdb;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Craken;
import net.bleujin.searcher.SearchControllerConfig;
import net.ion.framework.db.DBController;
import net.ion.framework.util.Debug;

public class TestCache extends TestStdMethod{

	
	@Test
	public void testNormal() throws Exception {
		DBController dc = ((PGCraken)c).dc() ;
		final AtomicInteger ai = new AtomicInteger() ;
		dc.addServant(atask -> {
			ai.incrementAndGet() ; 
			Debug.debug(atask.getQueryable().getProcSQL(), "called") ;	
		});
		
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").merge() ;
			wsession.pathBy("/emp/jin").property("name", "jin").merge() ;
			wsession.pathBy("/emp/hero").property("name", "hero").merge() ;
		}) ;
		
		rsession.pathBy("/emp/bleujin").debugPrint() ;
		rsession.pathBy("/emp/hero").debugPrint() ;
		rsession.pathBy("/emp").children().debugPrint() ;
		
		assertEquals(ai.get(), 6 + 1 + 1 + 2 + 3);
	}

	@Test
	public void testCache() throws Exception {
		DBController dc = ((PGCraken)c).dc() ;
		final AtomicInteger ai = new AtomicInteger() ;
		dc.addServant(atask -> {
			ai.incrementAndGet() ; 
			Debug.debug(atask.getQueryable().getProcSQL(), "called") ;	
		});

		Craken craken = ((PGCraken)c).cached(1000) ;
		
		rsession = craken.login("testworkspace") ;
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "new bleujin").merge() ;
			wsession.pathBy("/emp/jin").property("name", "new jin").merge() ;
			wsession.pathBy("/emp/hero").property("name", "new hero").merge() ;
		}) ;
		
		rsession.pathBy("/emp/bleujin").debugPrint() ;
		rsession.pathBy("/emp/hero").debugPrint() ;
		rsession.pathBy("/emp").children().debugPrint() ; // /emp
		
		assertEquals(ai.get(), 6 + 2);
	}

	@Test
	public void testSearch() throws Exception {
		Craken craken = ((PGCraken)c).cached(1000) ;
		
		rsession = craken.login("testworkspace") ;
		rsession.workspace().indexCntral(SearchControllerConfig.newRam().build()) ;
		
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "new bleujin").merge() ;
			wsession.pathBy("/emp/jin").property("name", "new jin").merge() ;
			wsession.pathBy("/emp/hero").property("name", "new hero").merge() ;
		}) ;
		
		rsession.pathBy("/emp").childQuery("").find().debugPrint() ;
		
	}
}


