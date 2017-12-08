package net.bleujin.rcraken;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.redisson.Redisson;
import org.redisson.api.RRemoteService;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class Craken {

	private Config config;
	private RedissonClient rclient;

	private ConcurrentMap<String, Workspace> wss = new ConcurrentHashMap<>();
	private CrakenNode cnode;

	public Craken(Config config) {
		this.config = config;
	}

	public Craken start() {
		this.rclient = Redisson.create(config);
		return this;
	}

	public ReadSession login(String wname) {
		return findWorkspace(wname).readSession();
	}

	public Stream<Workspace> workspaces(){
		return wss.values().stream() ;
	}
	
	private Workspace findWorkspace(String wname) {
		if (wname.startsWith("_"))
			throw new IllegalAccessError("illegal worksapce name");

		wss.putIfAbsent(wname, new Workspace(wname, rclient).init());
		return wss.get(wname);
	}

	public void destroySelf() {
		if (cnode != null) cnode.destorySelf(); 
		else rclient.shutdown();
	}

	public RRemoteService remoteService(String name) {
		return rclient.getRemoteService(name);
	}

	public CrakenNode node(Map<String, Integer> workers) {
		if (rclient == null) throw new IllegalStateException("craken not started");
		this.cnode = new CrakenNode(rclient, config, workers).start();
		return cnode ;
	}
	
}
