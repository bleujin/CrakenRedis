package net.bleujin.rcraken;

import java.util.Map;

import org.redisson.RedissonNode;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RReadWriteLock;
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

	CrakenNode start() {
        RedissonNodeConfig nodeConfig = new RedissonNodeConfig(config);
        nodeConfig.setExecutorServiceWorkers(workers);
        this.node = RedissonNode.create(nodeConfig);
        node.start();

		return this;
	}

	public RScheduledExecutorService executorService() {
		return executorService(CrakenConfig.DFT_WORKER_NAME) ;
	}
	
	public RScheduledExecutorService executorService(String workerName) {
		return rclient.getExecutorService(workerName);
	}
	
	public RReadWriteLock rwLock(String rwName) {
		return rclient.getReadWriteLock(rwName);
	}
	
	public RCountDownLatch countdownLatch(String cdName) {
		return rclient.getCountDownLatch(cdName);
	}

	void destorySelf() {
		node.shutdown(); 
	}


}
