package net.bleujin.rcraken;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.tbase.TestBaseCrakenRedis;
import net.ion.framework.util.Debug;

public class ReadChildrenTest extends TestBaseCrakenRedis {

	@Test
	public void readChildren() throws Exception {
		rsession.tran(SAMPLE)
			.thenAccept((s) ->{
			rsession.pathBy("/emp").children().debugPrint();
		}) ;
	}

	@Test
	public void testStream() throws Exception {
		rsession.tran(SAMPLE)
			.thenAccept((s) ->{
			rsession.pathBy("/emp").children().stream().limit(5).filter(rnode -> rnode.property("age").asLong() >= 25L)
			.sorted((n1, n2) -> n2.property("age").asInt() - n1.property("age").asInt())
			.forEach(System.out::println);
		}) ;


	}

	@Test
	public void testFlatMap() throws Exception {
		rsession.tran(SAMPLE)
			.thenAccept((s) ->{
			Arrays.asList(rsession.pathBy("/emp").children(), rsession.pathBy("/emp").children())
				.stream().flatMap(i -> i.stream()).forEach(System.out::println);
		}) ;
	}

	@Test
	public void testCollect() throws Exception {
		rsession.tran(SAMPLE)
			.thenAccept((s) ->{
			List<ReadNode> list = rsession.pathBy("/emp").children().stream().collect(Collectors.toList());
			for (ReadNode readNode : list) {
				Debug.line(readNode);
			}
		}) ;
	}

	@Test
	public void testWriteChildren() throws Exception {
		rsession.tran(SAMPLE)
			.thenAccept((s) ->{
			rsession.tran(wsession -> {
				wsession.pathBy("/emp").children().stream().limit(2).map(wn -> wn.property("age", wn.property("age").asInt() * 2)).forEach(w -> w.merge());
				return null;
			});

			rsession.pathBy("/emp").children().debugPrint();
		}) ;
	}

}
