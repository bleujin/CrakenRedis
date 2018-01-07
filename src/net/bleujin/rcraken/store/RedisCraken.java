package net.bleujin.rcraken.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.redisson.Redisson;
import org.redisson.api.RRemoteService;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.ReadSession;

public class RedisCraken extends Craken{

	private Config config;
	private RedissonClient rclient;

	private ConcurrentMap<String, RedisWorkspace> wss = new ConcurrentHashMap<>();
	private CrakenNode cnode;
	private Map<String, Integer> workers;

	public RedisCraken(Config config, Map<String, Integer> workers) {
		this.config = config;
		this.workers = workers ;
	}

	public Craken start() {
		return start(true);
	}

	public RedisCraken start(boolean doStartNodeService) {
		this.rclient = Redisson.create(config);
		if (doStartNodeService) this.cnode = new RedisNode(rclient, config, workers).start();
		return this;
	}

	
	public ReadSession login(String wname) {
		return findWorkspace(wname).readSession();
	}

	protected RedisWorkspace findWorkspace(String wname) {
		if (wname.startsWith("_"))
			throw new IllegalAccessError("illegal worksapce name");

		wss.putIfAbsent(wname, (RedisWorkspace)new RedisWorkspace(wname, rclient).init());
		return wss.get(wname);
	}

	public void shutdown() {
		if (cnode != null) cnode.shutdown(); 
		rclient.shutdown();
	}
	
	@Deprecated // test only
	public void removeAll() {
		rclient.getKeys().deleteByPattern("*") ;
	}

	public RRemoteService remoteService(String name) {
		return rclient.getRemoteService(name);
	}

	public CrakenNode node() {
		if (this.rclient == null || this.cnode == null) throw new IllegalStateException("craken node not started");
		return cnode ;
	}
	
	
	public RedissonClient rclient() {
		return rclient ;
	}
}
