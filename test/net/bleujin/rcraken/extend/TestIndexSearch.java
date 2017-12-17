package net.bleujin.rcraken.extend;

import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.util.Debug;
import net.ion.nsearcher.config.Central;
import net.ion.nsearcher.config.CentralConfig;

public class TestIndexSearch extends TestBaseCrakenRedis {

	
	public void testFirst() throws Exception {
		Central central = CentralConfig.newRam().build() ;
		
		rsession.workspace().indexCntral(central) ;
		rsession.tran(SAMPLE) ;

		Thread.sleep(500); // index operated asynchronously(listener)
		rsession.pathBy("/emp").childQuery("", true).find().toRows("name, age").debugPrint();
		rsession.pathBy("/emp").childQuery("", true).find().stream().forEach(System.out::println); ;
	}
	
	public void testIndex() throws Exception {
		Central central = CentralConfig.newRam().build() ;
		
		rsession.workspace().indexCntral(central) ;
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
	
	
	public void testSearchInWriteSession() throws Exception {
		Central central = CentralConfig.newRam().build() ;
		
		rsession.workspace().indexCntral(central) ;
		rsession.tran(SAMPLE) ;
		
		Thread.sleep(500);
		rsession.tran(wsession ->{
			wsession.readSession().pathBy("/emp").childQuery("age:[25 TO 30]").find()
				.stream(wsession).map(wn-> wn.property("age", wn.property("age").asInt() * 2)).forEach(wn -> wn.merge()); 
			return null ;
		}) ;
		
		rsession.pathBy("/emp").children().debugPrint(); // hero, jin 
	}
	
}
