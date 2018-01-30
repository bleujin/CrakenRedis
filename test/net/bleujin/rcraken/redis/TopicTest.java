package net.bleujin.rcraken.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

import net.bleujin.rcraken.extend.Topic;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.util.Debug;

public class TopicTest extends TestBaseRCraken {

	@Test
	public void createTopic() throws Exception {
		
        CountDownLatch latch = new CountDownLatch(1);
        Topic<String> topic = rsession.workspace().topic("topic2");
        topic.addListener(new MessageListener<String>() {
            @Override
            public void onMessage(String channel, String msg) {
            	Debug.line(channel);
            	assertEquals("msg", msg);
                latch.countDown();
            }
        });
        
        topic.publish("msg");
        latch.await();
	}
}
