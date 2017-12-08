package net.bleujin.rcraken.extend;

import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

public class Topic<T> {

	private String name;
	private RTopic<T> inner;

	public Topic(String name, RTopic<T> rtopic) {
		this.name = name ;
		this.inner = rtopic ;
	}

	public String name() {
		return name ;
	}
	
	public Topic<T> addListener(MessageListener<T> mlistener) {
		inner.addListener(mlistener) ;
		return this ;
	}

	public long publish(T message) {
		return inner.publish(message) ;
	}

}
