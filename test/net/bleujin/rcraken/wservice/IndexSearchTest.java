package net.bleujin.rcraken.wservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.redis.TestBaseRedis;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.util.Debug;
import net.ion.nsearcher.config.Central;
import net.ion.nsearcher.config.CentralConfig;

public class IndexSearchTest extends TestBaseRCraken {

	
	@Test
	public void indexWithEachCraken() throws Exception {
		Central central = CentralConfig.newRam().build() ;
		rsession.workspace().indexCntral(central) ;
		
		rsession.tran(SAMPLE) ;

		Thread.sleep(500); // index operated asynchronously(listener)
		rsession.pathBy("/emp").childQuery("", true).find().toRows("name, age").debugPrint();
		rsession.pathBy("/emp").childQuery("", true).find().stream().forEach(System.out::println); ;
	}
	
	@Test
	public void indexWhenSettingCentral() throws Exception {
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
	
	
	@Test
	public void searchInWriteSession() throws Exception {
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
	
	
	@Test
	void reindex() throws Exception {
		rsession.tran(SAMPLE) ;
		
		Central central = CentralConfig.newRam().build() ;
		rsession.workspace().indexCntral(central).reindex(true) ;
		
		rsession.pathBy("/emp").childQuery("age:[25 TO 30]").find().debugPrint();
	}
}
