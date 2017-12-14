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
	
	
}
