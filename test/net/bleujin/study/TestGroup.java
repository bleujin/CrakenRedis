package net.bleujin.study;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RMapCache;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.MapEntryListener;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.ion.framework.util.Debug;

public class TestGroup extends TestBaseCrakenRedis {

	
	@Test
	public void testListMap() throws Exception {
		RListMultimap<String, String> mlist = c.rclient().getListMultimap("study.1") ;
		
		mlist.put("emp", "bleujin") ;
		mlist.put("emp", "hero") ;
		mlist.put("emp", "jin") ;
		
		RList<String> list = mlist.get("emp") ;
		list.stream().forEach(System.out::println);
	}
	
	@Test
	public void testListener() throws Exception {
		RMapCache<Object, Object> map = c.rclient().getMapCache("study.2") ;
//		map.put("not_contains", "dd") ;
		map.addListener(new EntryRemovedListener<String, String>() {
			public void onRemoved(EntryEvent<String, String> event) {
				Debug.debug(EventType.REMOVED, event.getKey(), event.getValue(), event.getOldValue());
			}
		}) ;
		
		map.remove("not_contains") ;
	}
	
	@Test
	public void testFqn() throws Exception {
		Fqn fqn = Fqn.from("__tranid") ;
		Debug.line(fqn);
	}
}
