package net.bleujin.rcraken.extend;

import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.RedissonNode;
import org.redisson.api.CronSchedule;
import org.redisson.api.RExecutorService;
import org.redisson.api.RMap;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;
import org.redisson.config.Config;
import org.redisson.config.RedissonNodeConfig;

import junit.framework.TestCase;
import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.ion.framework.util.Debug;

public class TestCrakenService extends TestCase {

	
	public void testCluster() throws Exception {
		Craken craken = CrakenConfig.redisCluster("redis://127.0.0.1:6379", "redis://127.0.0.1:6380").build().start() ;
		
		craken.destroySelf(); 
	}
	
	
	public void testRemoteService() {
		Craken server = CrakenConfig.redisSingle().build().start() ;
		Craken client = CrakenConfig.redisSingle().build().start() ;
	    try {
	        server.remoteService("bleujin.rs").register(RemoteInterface.class, new RemoteImpl());
	        RemoteInterface service = client.remoteService("bleujin.rs").get(RemoteInterface.class);
	        assertEquals(42, service.myMethod(21L).longValue());

	    } finally {
	        client.destroySelf();
	        server.destroySelf();
	    }
	}
	
	
	public void testExecutor() throws Exception {
        Craken craken = CrakenConfig.redisSingle().build().start() ;  // when craken is cluster mode, This service is worth it.
        RExecutorService es = craken.node(Collections.singletonMap("node.worker", 2)).executorService("node.worker") ;
        
        Debug.line(es.isShutdown(), es.isTerminated(), es.countActiveWorkers());
        es.execute(new RunnableTask());
        es.submit(new CallableTask()).get();
        
        es.shutdown();
        es.awaitTermination(3, TimeUnit.SECONDS) ;
        craken.destroySelf();
	}
	

	public void testScheduleExecutor() throws Exception {
        Craken craken = CrakenConfig.redisSingle().build().start() ;  // when craken is cluster mode, This service is worth it.
        RScheduledExecutorService es = craken.node(Collections.singletonMap("node.worker", 2)).executorService("node.worker") ;
        
        Debug.line(es.isShutdown(), es.isTerminated(), es.countActiveWorkers());
        es.schedule(new RunnableTask(), 10, TimeUnit.SECONDS);
        es.schedule(new CallableTask(), 4, TimeUnit.MINUTES);

        es.schedule(new RunnableTask(), CronSchedule.of("10 0/5 * * * ?"));
        es.schedule(new RunnableTask(), CronSchedule.dailyAtHourAndMinute(10, 5));
        es.schedule(new RunnableTask(), CronSchedule.weeklyOnDayAndHourAndMinute(12, 4, Calendar.MONDAY, Calendar.FRIDAY));
        
        es.shutdown();
        es.awaitTermination(3, TimeUnit.SECONDS) ;
        craken.destroySelf();
	}
	

	
	
	
	
	
	


	public interface RemoteInterface {
	    Long myMethod(Long value);
	}

	public class RemoteImpl implements RemoteInterface {

	    public RemoteImpl() {
	    }
	    
	    @Override
	    public Long myMethod(Long value) {
	        return value*2;
	    }

	}
	
    public static class RunnableTask implements Runnable {
        @RInject
        RedissonClient redisson;

        @Override
        public void run() {
            System.out.print("Completed");
        }
        
    }
    
    public static class CallableTask implements Callable<String> {
        @RInject
        RedissonClient redisson;
        
        @Override
        public String call() throws Exception {
            return "Hello";
        }
    }

}


