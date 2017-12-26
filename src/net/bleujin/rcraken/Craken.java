package net.bleujin.rcraken;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RRemoteService;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import net.bleujin.rcraken.store.RedisNode;

public abstract class Craken {

	public Craken start() {
		return start(true);
	}

	public abstract Craken start(boolean doStartNodeService) ;

	
	public ReadSession login(String wname) {
		return findWorkspace(wname).readSession();
	}

	protected abstract Workspace findWorkspace(String wname) ;
	
	public abstract void shutdownSelf() ;
	
	@Deprecated // test only
	public abstract void removeAll() ;

	public abstract CrakenNode node() ;
	

}
