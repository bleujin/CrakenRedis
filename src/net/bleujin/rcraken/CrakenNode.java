package net.bleujin.rcraken;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

public interface CrakenNode {

	CrakenNode start();

	public ScheduledExecutorService executorService();

	public ScheduledExecutorService executorService(String workerName);

	public ReadWriteLock rwLock(String rwName);

	// public CountDownLatch countdownLatch(String cdName) ;

	void shutdown();

}
