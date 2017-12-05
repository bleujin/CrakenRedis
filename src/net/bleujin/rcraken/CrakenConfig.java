package net.bleujin.rcraken;

import org.redisson.config.Config;

public class CrakenConfig {

	private Config config;

	private CrakenConfig() {
		this.config = new Config() ;
	}
	
	public static CrakenConfig redis() {
		return new CrakenConfig();
	}

	public CrakenConfig singleServer() {
		config.useSingleServer().setAddress("redis://127.0.0.1:6379") ;
		return this;
	}
	
	public CrakenConfig setAddress(String address) {
		config.useSingleServer().setAddress(address) ;
		return this ;
	}

	public Craken build() {
		return new Craken(config);
	}

}
