package net.bleujin.rcraken.store.rdb;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.bleujin.rcraken.CrakenNode;
import net.ion.framework.util.MapUtil;

public class PGNode implements CrakenNode {

	private Map<String, ScheduledExecutorService> ess = MapUtil.newMap() ;
	private Map<String, ReadWriteLock> rws = MapUtil.newMap() ;
	private Map<String, Integer> workers;
	
	public PGNode(Map<String, Integer> workers) {
		this.workers = workers ;
	}

	@Override
	public PGNode start() {
		return this;
	}

	@Override
	public ScheduledExecutorService executorService() {
		return executorService(workers.keySet().iterator().next()) ;
	}

	@Override
	public ScheduledExecutorService executorService(String workerName) {
		if (! workers.containsKey(workerName)) throw new IllegalAccessError("not found workerName :" + workerName) ;
		
		if (! ess.containsKey(workerName)) {
			ess.put(workerName, Executors.newScheduledThreadPool(workers.get(workerName))) ;
		}
		return ess.get(workerName) ;
	}

	@Override
	public ReadWriteLock rwLock(String rwName) {
		ReadWriteLock result = rws.getOrDefault(rwName, new ReentrantReadWriteLock(true)) ;
		synchronized (this) {
			if (! rws.containsKey(rwName)) rws.put(rwName, result) ;
		}
		return result ;
	}

	@Override
	public void shutdown() {
		ess.values().forEach( es -> es.shutdown());
	}

}
