package net.bleujin.rcraken;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.redisson.config.Config;

import net.bleujin.rcraken.store.MapConfig;
import net.bleujin.rcraken.store.RedisConfig;
import net.ion.framework.util.MapUtil;

public interface CrakenConfig {

	public static CrakenConfig redisSingle() {
		return RedisConfig.redisSingle() ;
	}

	public static CrakenConfig redisCluster(String... addresses) {
		return RedisConfig.redisCluster(addresses) ;
	}

	public static CrakenConfig redisSingle(String address) {
		return RedisConfig.redisSingle() ;
	}
	
	public static CrakenConfig redis(Config config) {
		return RedisConfig.redis(config) ;
	}

	public static CrakenConfig mapMemory() {
		return MapConfig.memory();
	}

	public Craken build()  ;



}
