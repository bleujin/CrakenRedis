package net.bleujin.rcraken;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.bleujin.searcher.SearchControllerConfig;
import net.ion.framework.util.Debug;

public class BatchTran extends TestBaseRCraken {

	@Test
	public void batchTran() throws Exception {
		rsession.workspace().indexCntral(SearchControllerConfig.newRam().build()) ;
		rsession.batch(bsession ->{
			for (int i = 0; i < 5; i++) {
				bsession.pathBy("/emp/" + i).property("index", i).property("type", "batch").insert(); 
			}
			bsession.pathBy("/emp/bleujin").property("address", "city").insert() ;// overwrite
			return true ;
		}) ;
		Thread.sleep(500);
		rsession.pathBy("/").walkBreadth().debugPrint();
	}


	@Test
	public void testBatchIndex() throws Exception {
		rsession.workspace().indexCntral(SearchControllerConfig.newRam().build()) ;
		rsession.batch(bsession ->{
			for (int i = 0; i < 10; i++) {
				bsession.pathBy("/emp/" + i).property("index", i).property("type", "batch").insert(); 
			}
			bsession.pathBy("/emp/bleujin").property("address", "city").insert() ;// overwrite
			return true ;
		}) ;
		Thread.sleep(500);
		rsession.pathBy("/emp").childQuery("").find().debugPrint();
	}

	
	@Test
	public void testBatch2() throws Exception {
		rsession.batch(bsession ->{
			for (int i = 0; i < 10; i++) {
				bsession.pathBy("/" + i).property("index", i).property("type", "batch").insert(); 
			}
			bsession.pathBy("/emp/bleujin").property("address", "city").insert() ;// overwrite
			return true ;
		}) ;
		rsession.pathBy("/").walkDepth().debugPrint();
	}


	
	@Test
	public void testConfirm() throws Exception {
		rsession.pathBy("/").children().debugPrint();
		Debug.line();
		rsession.pathBy("/emp").children().debugPrint();
	}
}
