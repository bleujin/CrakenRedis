package net.bleujin.rcraken;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

import org.redisson.api.RMap;

public interface CrakenNode {

	CrakenNode start() ;
	public ScheduledExecutorService executorService() ;
	
	public ScheduledExecutorService executorService(String workerName) ;
	
	public ReadWriteLock rwLock(String rwName) ;
	
//	public CountDownLatch countdownLatch(String cdName) ;

	void shutdown() ;

	public <T, R> RMap<T, R> getMap(String name) ;


}
