package net.bleujin.rcraken.extend;

import java.util.concurrent.CountDownLatch;

import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.util.Debug;

public class TestTopic extends TestBaseCrakenRedis {

	public void testCreate() throws Exception {
		
        CountDownLatch latch = new CountDownLatch(1);
        Topic<String> topic = rsession.workspace().topic("topic2");
        topic.addListener(new MessageListener<String>() {
            @Override
            public void onMessage(String channel, String msg) {
            	Debug.line(channel, msg);
                latch.countDown();
            }
        });
        
        topic.publish("msg");
        latch.await();
	}
}
