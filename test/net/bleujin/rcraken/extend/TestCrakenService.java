package net.bleujin.rcraken.extend;

import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.redisson.api.CronSchedule;
import org.redisson.api.RExecutorService;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;
import org.redisson.api.annotation.RRemoteAsync;

import junit.framework.TestCase;
import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.CrakenNode;
import net.ion.framework.util.Debug;
import net.ion.framework.util.InfinityThread;

public class TestCrakenService extends TestCase {

	public void testCluster() throws Exception {
		Craken craken = CrakenConfig.redisCluster("redis://127.0.0.1:6701", "redis://127.0.0.1:6702", "redis://127.0.0.1:6703").build().start();
		Thread.sleep(1000);
		craken.destroySelf();
	}

	public void testRemoteService() throws Exception {
		Craken server = CrakenConfig.redisSingle().build().start();
		Craken client = CrakenConfig.redisSingle().build().start();
		try {
			server.remoteService("bleujin.rs").register(RemoteInterface.class, new RemoteImpl());
			RemoteInterface service = client.remoteService("bleujin.rs").get(RemoteInterface.class);
			assertEquals(42, service.myMethod(21L).longValue());

		} finally {
			client.destroySelf();
			server.destroySelf();
		}
	}


	
	/*
	 * public void testRemoteServer() throws Exception { Craken server =
	 * CrakenConfig.redisSingle().build().start();
	 * server.remoteService("bleujin.rs").register(RemoteInterface.class, new
	 * RemoteImpl()); new InfinityThread().startNJoin(); }
	 * 
	 * public void testRemoteClient() throws Exception { Craken client =
	 * CrakenConfig.redisSingle().build().start(); RemoteInterface service =
	 * client.remoteService("bleujin.rs").get(RemoteInterface.class);
	 * assertEquals(42, service.myMethod(21L).longValue()); client.destroySelf(); }
	 */

	public void testExecutor() throws Exception {
		Craken craken = CrakenConfig.redisSingle().build().start();
		CrakenNode cnode = craken.node(Collections.singletonMap("node.worker", 2)).start();

		RExecutorService es = cnode.executorService("node.worker");

		Debug.line(es.isShutdown(), es.isTerminated(), es.countActiveWorkers());
		es.execute(new RunnableTask());
		es.submit(new CallableTask()).get();

		// es.shutdown();
		es.awaitTermination(3, TimeUnit.SECONDS);
		craken.destroySelf();
	}

	public void testScheduleExecutor() throws Exception {
		Craken craken = CrakenConfig.redisSingle().build().start();
		CrakenNode cnode = craken.node(Collections.singletonMap("node.sworker", 2)).start();

		RScheduledExecutorService es = cnode.executorService("node.sworker");

		Debug.line(es.isShutdown(), es.isTerminated(), es.countActiveWorkers());
		es.schedule(new RunnableTask(), 10, TimeUnit.SECONDS);
		es.schedule(new CallableTask(), 4, TimeUnit.MINUTES);

		es.schedule(new RunnableTask(), CronSchedule.of("10 0/5 * * * ?"));
		es.schedule(new RunnableTask(), CronSchedule.dailyAtHourAndMinute(10, 5));
		es.schedule(new RunnableTask(), CronSchedule.weeklyOnDayAndHourAndMinute(12, 4, Calendar.MONDAY, Calendar.FRIDAY));

		new InfinityThread().startNJoin();

		es.shutdown();
		es.awaitTermination(3, TimeUnit.SECONDS);
		craken.destroySelf();
	}

	public interface RemoteInterface {
		Long myMethod(Long value) throws Exception;
	}

	public class RemoteImpl implements RemoteInterface {

		public RemoteImpl(){
		}

		@Override
		public Long myMethod(Long value) throws Exception {
			return value * 2;
		}
	}

	public static class RunnableTask implements Runnable {
		@RInject
		RedissonClient redisson;

		@Override
		public void run() {
			System.out.print("Hello World" + redisson);
		}

	}

	public static class CallableTask implements Callable<String> {
		@RInject
		RedissonClient redisson;

		@Override
		public String call() throws Exception {
			return "Hello World";
		}
	}

}
