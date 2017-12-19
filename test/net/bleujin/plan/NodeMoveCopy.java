package net.bleujin.plan;

import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.TestBaseCrakenRedis;

public class NodeMoveCopy extends TestBaseCrakenRedis {
	

	public void testCopy() throws Exception {
		rsession.tran(SAMPLE) ;

		ReadSession rs2 = c.login("testwpace2") ;
		
		rsession.tran(wsession ->{
			
			return null ;
		}) ;
		
//		rsession.pathBy("/emp").copyTo(rs2.pathBy("/")) ;
		
	}

	public void testMove() throws Exception {
		
	}
	

}
