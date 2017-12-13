package net.bleujin.plan;

import java.util.Map;
import java.util.Set;

import org.redisson.api.RSetMultimap;

import com.sun.corba.se.spi.orbutil.threadpool.Work;

import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.util.Debug;

public class TestBatch extends TestBaseCrakenRedis {

	
	public void testBatch() throws Exception {
		rsession.batch(bsession ->{
			for (int i = 0; i < 10; i++) {
				bsession.pathBy("/emp/" + i).property("index", i).property("type", "batch").insert(); 
			}
		}) ;
		rsession.pathBy("/").walkBreadth().debugPrint();
	}
}
