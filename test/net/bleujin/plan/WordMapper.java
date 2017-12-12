package net.bleujin.plan;

import org.redisson.api.mapreduce.RCollector;
import org.redisson.api.mapreduce.RMapper;

import net.ion.framework.util.Debug;

public class WordMapper implements RMapper<String, String, String, Integer> {
	@Override
	public void map(String key, String value, RCollector<String, Integer> collector) {
		String[] words = value.split("[^a-zA-Z]");
		for (String word : words) {
			Debug.line();

			collector.emit(word, 1);
		}
	}
}