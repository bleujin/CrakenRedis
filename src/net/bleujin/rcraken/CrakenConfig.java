package net.bleujin.rcraken;

import java.io.File;
import java.util.Map;

import net.bleujin.rcraken.store.MapConfig;
import net.bleujin.rcraken.store.RedisConfig;
import net.bleujin.rcraken.store.rdb.PGConfig;

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
	
	public static PGConfig pgDB(String jdbcURL, String userId, String userPwd, File lobRootDir) {
		return new PGConfig().jdbcURL(jdbcURL).userId(userId).userPwd(userPwd).lobRootDir(lobRootDir) ;
	}

	public Craken build()  ;

	public Craken build(Map<String, Integer> workers);

	public static CrakenSwarm makeSwarm(String wname, CrakenConfig cconfig) {
		return CrakenSwarm.create().makeSwarm(wname, cconfig);
	}



}
