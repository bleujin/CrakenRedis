package net.bleujin.rcraken;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.redisson.config.Config;

import net.ion.framework.util.MapUtil;

public class CrakenConfig {

	public final static String DFT_WORKER_NAME = "craken.worker" ;
	
	private final Config config;
	private Map<String, Integer> workers = Collections.singletonMap(DFT_WORKER_NAME, 2) ;

	private CrakenConfig(Config config) {
		this.config = config;
	}

	public static CrakenConfig redisSingle() {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://127.0.0.1:6379");
		return new CrakenConfig(config);
	}

	public static CrakenConfig redisCluster(String... addresses) {
		Config config = new Config();
		config.useClusterServers().addNodeAddress(addresses) ;
		return new CrakenConfig(config) ;
	}

	
	public static CrakenConfig redisSingle(String address) {
		Config config = new Config();
		config.useSingleServer().setAddress(address);
		return new CrakenConfig(config);
	}

	
	public static CrakenConfig redis(Config config) {
		CrakenConfig result = new CrakenConfig(config) ;
		return result ;
	}

	
	public CrakenConfig worker(Map<String, Integer> workers) {
		
		if (! workers.containsKey(DFT_WORKER_NAME)) {
			Map<String, Integer> map = new HashedMap(workers) ;
			map.put(DFT_WORKER_NAME, 2) ;
			this.workers = map ;
		} else {
			this.workers = workers ;
		}
		return this ;
	}
	
	public Craken build() {
		return new Craken(config, this.workers);
	}


}
