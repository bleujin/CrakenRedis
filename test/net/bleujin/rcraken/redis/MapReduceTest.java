package net.bleujin.rcraken.redis;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.RedissonNode;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.mapreduce.RCollator;
import org.redisson.api.mapreduce.RCollector;
import org.redisson.api.mapreduce.RMapReduce;
import org.redisson.api.mapreduce.RMapper;
import org.redisson.api.mapreduce.RReducer;
import org.redisson.config.Config;
import org.redisson.config.RedissonNodeConfig;

import junit.framework.TestCase;
import net.bleujin.rcraken.store.RedisNode;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.util.Debug;

public class MapReduceTest extends TestBaseRCraken {
	// https://github.com/redisson/redisson/wiki/9.-distributed-services

	@Test
	public void testFirst() throws Exception {
		RMap<String, String> map = ((RedisNode)c.node()).getMap("mapreduce");
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
		mapToNumber.entrySet().forEach(System.out::println);
		Debug.line(totalWordsAmount);
	}

}

class WordMapper implements RMapper<String, String, String, Integer> {
	@Override
	public void map(String key, String value, RCollector<String, Integer> collector) {
		String[] words = value.split("[^a-zA-Z]");
		for (String word : words) {
			collector.emit(word, 1);
		}
	}
}

class WordReducer implements RReducer<String, Integer> {
	@Override
	public Integer reduce(String reducedKey, Iterator<Integer> iter) {
		int sum = 0;
		while (iter.hasNext()) {
			Integer i = (Integer) iter.next();
			sum += i;
		}
		return sum;
	}
}

class WordCollator implements RCollator<String, Integer, Integer> {
	@Override
	public Integer collate(Map<String, Integer> resultMap) {
		int result = 0;
		for (Integer count : resultMap.values()) {
			result += count;
		}
		return result;
	}

}
