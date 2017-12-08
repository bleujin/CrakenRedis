package net.bleujin.rcraken;

import java.util.Collections;
import java.util.Map;

import org.redisson.RedissonNode;
import org.redisson.api.RExecutorService;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.RedissonNodeConfig;

public class CrakenNode {

	private RedissonClient rclient;
	private Config config;
	private Map<String, Integer> workers;
	private RedissonNode node;

	public CrakenNode(RedissonClient rclient, Config config, Map<String, Integer> workers) {
		this.rclient = rclient ;
		this.config = config ;
		this.workers = workers ;
	}

	public CrakenNode start() {
        RedissonNodeConfig nodeConfig = new RedissonNodeConfig(config);
        nodeConfig.setExecutorServiceWorkers(workers);
        this.node = RedissonNode.create(nodeConfig);
        node.start();
		return this;
	}

	public RScheduledExecutorService executorService(String workerName) {
		return rclient.getExecutorService(workerName);
	}

	public void destorySelf() {
		node.shutdown(); 
	}


}
