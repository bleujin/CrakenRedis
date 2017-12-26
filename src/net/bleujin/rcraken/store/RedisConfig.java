package net.bleujin.rcraken.store;

import java.util.Collections;
import java.util.Map;

import org.redisson.config.Config;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;

public class RedisConfig implements CrakenConfig {

	private final Config config;

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

	public Craken build() {
		return new RedisCraken(config, Collections.singletonMap(DFT_WORKER_NAME, 3));
	}

	@Override
	public Craken build(Map<String, Integer> workers) {
		return new RedisCraken(config, workers);
	}

}
