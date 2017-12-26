package net.bleujin.rcraken.store;

import java.util.Map;

import org.redisson.RedissonNode;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.RedissonNodeConfig;

import net.bleujin.rcraken.CrakenNode;

public class RedisNode implements CrakenNode{
	
	private RedissonClient rclient;
	private Config config;
	private Map<String, Integer> workers;
	private RedissonNode node;

	public RedisNode(RedissonClient rclient, Config config, Map<String, Integer> workers) {
		this.rclient = rclient ;
		this.config = config ;
		this.workers = workers ;
	}

	public RedisNode start() {
        RedissonNodeConfig nodeConfig = new RedissonNodeConfig(config);
        nodeConfig.setExecutorServiceWorkers(workers);
        this.node = RedissonNode.create(nodeConfig);
        node.start();

		return this;
	}

	public RScheduledExecutorService executorService() {
		return executorService(workers.keySet().iterator().next()) ;
	}
	
	public RScheduledExecutorService executorService(String workerName) {
		if (! workers.containsKey(workerName)) throw new IllegalAccessError("not found workerName :" + workerName) ;
		return rclient.getExecutorService(workerName);
	}
	
	public RReadWriteLock rwLock(String rwName) {
		return rclient.getReadWriteLock(rwName);
	}
	
	public RCountDownLatch countdownLatch(String cdName) {
		return rclient.getCountDownLatch(cdName);
	}

	public void shutdown() {
		node.shutdown(); 
	}

	public <T, R> RMap<T, R> getMap(String name) {
		return rclient.getMap(name);
	}
}
