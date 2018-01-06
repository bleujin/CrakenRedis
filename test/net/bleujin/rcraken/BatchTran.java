package net.bleujin.rcraken;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBatch;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RMap;
import org.redisson.api.RMapAsync;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetMultimap;
import org.redisson.api.mapreduce.RCollator;

import com.sun.corba.se.spi.orbutil.threadpool.Work;

import net.bleujin.rcraken.extend.NodeListener;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.bleujin.rcraken.tbase.TestBaseCrakenRedis;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.nsearcher.config.CentralConfig;

public class BatchTran extends TestBaseCrakenRedis {

	@Test
	public void batchTran() throws Exception {
		rsession.workspace().indexCntral(CentralConfig.newRam().build()) ;
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
		rsession.workspace().indexCntral(CentralConfig.newRam().build()) ;
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
