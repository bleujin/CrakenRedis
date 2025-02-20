package net.bleujin.rcraken.wservice;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.bleujin.searcher.SearchController;
import net.bleujin.searcher.SearchControllerConfig;

public class IndexSearchTest extends TestBaseRCraken {

	
	@Test
	public void indexWithEachCraken() throws Exception {
		SearchController central = SearchControllerConfig.newRam().build() ;
		rsession.workspace().indexCentral(central) ;
		
		rsession.tran(SAMPLE) ;

		Thread.sleep(500); // index operated asynchronously(listener)
		rsession.pathBy("/emp").childQuery("", true).find().toRows("name, age").debugPrint();
		rsession.pathBy("/emp").childQuery("", true).find().stream().forEach(System.out::println); ;
	}
	
	@Test
	public void indexWhenSettingCentral() throws Exception {
		SearchController central = SearchControllerConfig.newRam().build() ;

		rsession.workspace().indexCentral(central) ;
		for (int i = 0; i < 10; i++) {
			rsession.tran(SAMPLE) ;
		}
		rsession.tran( wsession -> {
			wsession.pathBy("/emp/bleujin").removeSelf();
			return null ;
		});

		Thread.sleep(500);
		rsession.workspace().central().newSearcher().createRequest("").find().debugPrint("name", "age"); 
	}
	
	
	@Test
	public void searchInWriteSession() throws Exception {
		SearchController central = SearchControllerConfig.newRam().build() ;
		rsession.workspace().indexCentral(central) ;
		rsession.tran(SAMPLE) ;
		
		Thread.sleep(500);
		rsession.tran(wsession ->{
			wsession.readSession().pathBy("/emp").childQuery("age:[25 TO 30]").find()
				.stream(wsession).map(wn-> wn.property("age", wn.property("age").asInt() * 2)).forEach(wn -> wn.merge()); 
			return null ;
		}) ;
		
		rsession.pathBy("/emp").children().debugPrint(); // hero, jin 
	}
	
	
	@Test
	void reindex() throws Exception {
		rsession.tran(SAMPLE) ;
		
		SearchController central = SearchControllerConfig.newRam().build() ;
		rsession.workspace().indexCentral(central).reindex(true) ;
		
		rsession.pathBy("/emp").childQuery("age:[25 TO 30]").find().debugPrint();
	}
}
