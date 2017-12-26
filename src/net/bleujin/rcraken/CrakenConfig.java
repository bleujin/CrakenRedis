package net.bleujin.rcraken;

import java.io.File;
import java.util.Map;

import net.bleujin.rcraken.store.MapConfig;
import net.bleujin.rcraken.store.RedisConfig;

public interface CrakenConfig {
	public final static String DFT_WORKER_NAME = "craken.worker";

	public static CrakenConfig redisSingle() {
		return RedisConfig.redisSingle() ;
	}

	public static CrakenConfig redisCluster(String... addresses) {
		return RedisConfig.redisCluster(addresses) ;
	}

	public static CrakenConfig redisSingle(String address) {
		return RedisConfig.redisSingle() ;
	}
	
	public static CrakenConfig mapMemory() {
		return MapConfig.memory() ;
	}

	public static CrakenConfig mapFile(File file) {
		return MapConfig.file(file) ;
	}

	public Craken build()  ;

	public Craken build(Map<String, Integer> workers);



}
