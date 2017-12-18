package net.bleujin.rcraken.distribute;

import java.util.Iterator;

import org.redisson.api.mapreduce.RReducer;

import net.ion.framework.util.Debug;

public class WordReducer implements RReducer<String, Integer> {
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
