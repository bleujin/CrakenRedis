package net.bleujin.rcraken.store.rdb;

import org.junit.jupiter.api.Test;

import net.bleujin.searcher.SearchController;
import net.bleujin.searcher.SearchControllerConfig;

public class TestSearch extends TestStdMethod {

	
	@Test
	public void testQuery() throws Exception {

		SearchController central = SearchControllerConfig.newRam().build() ;
		rsession.workspace().indexCntral(central) ;
		
		rsession.tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).merge() ;
			wsession.pathBy("/emp/hero").property("name", "hero").property("age", 30).merge() ;
			wsession.pathBy("/emp/jin").property("name", "jin").property("age", 40).merge() ;
		}) ;
		
		rsession.pathBy("/emp").childQuery("bleujin").find().debugPrint() ;
	}
	
}
