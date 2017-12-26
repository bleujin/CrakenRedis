package net.bleujin.rcraken.store;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.redisson.config.Config;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;

public class RedisConfig implements CrakenConfig {
	public final static String DFT_WORKER_NAME = "craken.worker";

	private final Config config;
	private Map<String, Integer> workers = Collections.singletonMap(DFT_WORKER_NAME, 2);

	public RedisConfig(Config config) {
		this.config = config;
	}

	public static RedisConfig redisSingle() {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://127.0.0.1:6379");
		return new RedisConfig(config);
	}

	public static RedisConfig redisCluster(String... addresses) {
		Config config = new Config();
		config.useClusterServers().addNodeAddress(addresses);
		return new RedisConfig(config);
	}

	public static RedisConfig redisSingle(String address) {
		Config config = new Config();
		config.useSingleServer().setAddress(address);
		return new RedisConfig(config);
	}

	public static RedisConfig redis(Config config) {
		RedisConfig result = new RedisConfig(config);
		return result;
	}

	public RedisConfig worker(Map<String, Integer> workers) {

		if (!workers.containsKey(DFT_WORKER_NAME)) {
			Map<String, Integer> map = new HashedMap(workers);
			map.put(DFT_WORKER_NAME, 2);
			this.workers = map;
		} else {
			this.workers = workers;
		}
		return this;
	}

	public Craken build() {
		return new RedisCraken(config, this.workers);
	}

}
