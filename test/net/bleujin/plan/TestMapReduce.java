package net.bleujin.plan;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.mapreduce.RCollator;
import org.redisson.api.mapreduce.RCollector;
import org.redisson.api.mapreduce.RMapReduce;
import org.redisson.api.mapreduce.RMapper;
import org.redisson.api.mapreduce.RReducer;

import junit.framework.TestCase;
import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.util.Debug;

public class TestMapReduce extends TestCase {
	// https://github.com/redisson/redisson/wiki/9.-distributed-services

	public void testFirst() throws Exception {
		RedissonClient redisson = Redisson.create();
		redisson.getMap("wordsMap").delete() ;
		
		RMap<String, String> map = redisson.getMap("wordsMap");
		map.put("line1", "Alice was beginning to get very tired");
		map.put("line2", "of sitting by her sister on the bank and");
		map.put("line3", "of having nothing to do once or twice she");
		map.put("line4", "had peeped into the book her sister was reading");
		map.put("line5", "but it had no pictures or conversations in it");
		map.put("line6", "and what is the use of a book");
		map.put("line7", "thought Alice without pictures or conversation");

		RMapReduce<String, String, String, Integer> mapReduce = map.<String, Integer>mapReduce().mapper(new WordMapper()).reducer(new WordReducer()).timeout(60, TimeUnit.SECONDS);;
		Map<String, Integer> mapToNumber = mapReduce.execute();
		Integer totalWordsAmount = mapReduce.execute(new WordCollator());
		Debug.line(mapToNumber);
		Debug.line(totalWordsAmount);
		
		redisson.shutdown(); 
	}

}




