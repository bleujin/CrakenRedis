package net.bleujin.rcraken.cservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import net.bleujin.rcraken.store.RedisConfig;
import net.bleujin.rcraken.store.RedisCraken;
import net.ion.framework.util.Debug;
import net.ion.framework.util.InfinityThread;
import net.ion.framework.util.MapUtil;

public class CrakenExecutorTest {

	@Test @Disabled("cluster mode redis needed")
	public void testCluster() throws Exception {
		Craken craken = CrakenConfig.redisCluster("redis://127.0.0.1:6701", "redis://127.0.0.1:6702", "redis://127.0.0.1:6703").build().start();
		Thread.sleep(1000);
		craken.shutdown();
	}

	@Test
	public void testRemoteService() throws Exception {
		RedisCraken server = (RedisCraken) CrakenConfig.redisSingle().build().start();
		RedisCraken client = (RedisCraken) CrakenConfig.redisSingle().build().start();
		try {
			server.remoteService("bleujin.rs").register(RemoteInterface.class, new RemoteImpl());
			RemoteInterface service = client.remoteService("bleujin.rs").get(RemoteInterface.class);
			assertEquals(42, service.myMethod(21L).longValue());

		} finally {
			client.shutdown();
			server.shutdown();
		}
	}


	@Test
	public void testExecutor() throws Exception {
		Craken craken = CrakenConfig.redisSingle().build().start();
		CrakenNode cnode = craken.node() ;

		ExecutorService es = cnode.executorService(RedisConfig.DFT_WORKER_NAME);
		

		es.execute(new RunnableTask());
		es.submit(new CallableTask()).get();

		es.awaitTermination(1, TimeUnit.SECONDS);
		craken.shutdown();
	}

	@Test
	public void testScheduleExecutor() throws Exception {
		Craken craken = CrakenConfig.redisSingle().build(MapUtil.create("node.sworker", 2)).start();
		CrakenNode cnode = craken.node();

		RScheduledExecutorService es = (RScheduledExecutorService)cnode.executorService("node.sworker");

		es.schedule(new RunnableTask(), 10, TimeUnit.SECONDS);
		es.schedule(new CallableTask(), 4, TimeUnit.MINUTES);

		es.schedule(new RunnableTask(), CronSchedule.of("10 0/5 * * * ?"));
		es.schedule(new RunnableTask(), CronSchedule.dailyAtHourAndMinute(10, 5));
		es.schedule(new RunnableTask(), CronSchedule.weeklyOnDayAndHourAndMinute(12, 4, Calendar.MONDAY, Calendar.FRIDAY));

		es.awaitTermination(1, TimeUnit.SECONDS);
		craken.shutdown();
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
