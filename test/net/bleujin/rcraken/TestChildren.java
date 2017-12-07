package net.bleujin.rcraken;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.ion.framework.util.Debug;

public class TestChildren extends TestBaseCrakenRedis {

	public void testChildren() throws Exception {
		rsession.tran(SAMPLE) ;
		
		rsession.pathBy("/emp").children().debugPrint();
	}
	
	
	public void testStream() throws Exception {
		rsession.tran(SAMPLE) ;

		rsession.pathBy("/emp").children().stream()
			.limit(5)
			.filter(rnode-> rnode.property("age").asLong() >= 25L)
			.sorted((n1, n2) -> n2.property("age").asInt() - n1.property("age").asInt())
			.forEach(System.out::println);
		
	}
	
	public void testFlatMap() throws Exception {
		rsession.tran(SAMPLE) ;
		
		Arrays.asList(rsession.pathBy("/emp").children(), rsession.pathBy("/emp").children())
		.stream().flatMap(i -> i.stream())
		.forEach(System.out::println);
	}
	
	public void testCollect() throws Exception {
		rsession.tran(SAMPLE) ;
		
		List<ReadNode> list = rsession.pathBy("/emp").children().stream().collect(Collectors.toList()) ;
		for (ReadNode readNode : list) {
			Debug.line(readNode);
		}
	}
	
	
	public void testWriteChildren() throws Exception {
		
		rsession.tran(SAMPLE) ;
		
		rsession.tran(wsession ->{
			wsession.pathBy("/emp").children().stream().map(wn -> wn.property("age", 30)).forEach(w -> w.merge()); ;
			return null ;
		}) ;
		
		rsession.pathBy("/emp").children().debugPrint();
	}
	

}
