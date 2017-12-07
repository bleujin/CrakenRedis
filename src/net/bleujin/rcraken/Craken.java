package net.bleujin.rcraken;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class Craken {

	private Config config;
	private RedissonClient rclient;
	
	private ConcurrentMap<String, Workspace> wss = new ConcurrentHashMap<>() ;

	public Craken(Config config) {
		this.config = config ;
	}

	
	public Craken start() {
		this.rclient =  Redisson.create(config);
		return this ;
	}


	public ReadSession login(String wname) {
		return findWorkspace(wname).readSession();
	}


	private Workspace findWorkspace(String wname) {
		if (wname.startsWith("_")) throw new IllegalAccessError("illegal worksapce name") ;
		
		wss.putIfAbsent(wname, new Workspace(wname, rclient).init()) ;
		return wss.get(wname) ;
	}


	public void destroySelf() {
		rclient.shutdown(); 
	}


}
