package net.bleujin.rcraken;

import org.redisson.config.Config;

public class CrakenConfig {

	private final Config config;

	private CrakenConfig(Config config) {
		this.config = config;
	}

	public static CrakenConfig redisSingle() {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://127.0.0.1:6379");
		return new CrakenConfig(config);
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

	public Craken build() {
		return new Craken(config);
	}

}
