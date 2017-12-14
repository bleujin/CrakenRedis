package net.bleujin.study;

import org.redisson.api.RBatch;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RMap;
import org.redisson.api.RMapAsync;

import junit.framework.TestCase;
import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.util.Debug;

public class TestStudy extends TestBaseCrakenRedis {
	

	public void testMap() throws Exception {
		Debug.line(c.rclient().getMapCache("mytest", rsession.workspace().mapOption()).delete()); 
		RMap<String, String> cmap = c.rclient().getMapCache("mytest") ;
		
		cmap.fastPut("/emp0", "{'name':'bleujin', 'age':10}") ;
		cmap.fastPut("/emp1", "{'name':'bleujin', 'age':10}") ;
		cmap.fastPut("/emp2", "{'name':'bleujin', 'age':10}") ;
		cmap.fastPut("/emp3", "{'name':'bleujin', 'age':10}") ;
		
		RBatch batch = c.rclient().createBatch() ;
		RMapAsync<String, String> bmap = batch.getMapCache("mytest") ;
		bmap.fastPutAsync("/emp3", "{'name':'bleujin', 'age':20}") ;
		bmap.fastPutAsync("/emp4", "{'name':'bleujin', 'age':10}") ;
		bmap.fastPutAsync("/emp5", "{'name':'bleujin', 'age':10}") ;
		batch.execute() ;
		
//		cmap = c.rclient().getLocalCachedMap("mytest", rsession.workspace().mapOption()) ;
		Debug.line(cmap.get("/emp1"), cmap.get("/emp3"), cmap.get("/emp5")) ;
		
	}
}
