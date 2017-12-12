package net.bleujin.plan;

import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.util.Debug;
import net.ion.nsearcher.config.Central;
import net.ion.nsearcher.config.CentralConfig;

public class TestIndexSearch extends TestBaseCrakenRedis {

	public void testIndex() throws Exception {
		
		Central central = CentralConfig.newRam().build() ;
		
		rsession.workspace().indexCntral(central) ;
		rsession.tran(SAMPLE) ;
		rsession.tran( wsession -> {
			wsession.pathBy("/emp/bleujin").removeSelf();
			return null ;
		}).get() ;
		
		rsession.workspace().central().newSearcher().createRequest("").find().debugPrint("name", "age"); 

		rsession.pathBy("/").childQuery("", true).find().toRows("name, age").debugPrint();
	}
	
	
}
