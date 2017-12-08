package net.bleujin.rcraken.extend;

import org.redisson.api.RAtomicLong;

public class Sequence {

	private final String name;
	private final RAtomicLong inner ;
	public Sequence(String name, RAtomicLong inner) {
		this.name = name ;
		this.inner = inner ;
	}

	public String name() {
		return name ;
	}
	
	public long get() {
		return inner.get() ;
	}
	
	public void set(long newValue) {
		inner.set(newValue);
	}
	
	public long incrementAndGet() {
		return inner.incrementAndGet() ;
	}

	public long decrementAndGet() {
		return inner.decrementAndGet() ;
	}
	public long getAndIncrement() {
		return inner.getAndIncrement() ;
	}
	public long getAndDecrement() {
		return inner.getAndDecrement() ;
	}
	public void delete() {
		inner.delete() ;
	}
	
	public long addAndGet(long delta){
		return inner.addAndGet(delta) ;
	}

	public long getAndAdd(long delta){
		return inner.getAndAdd(delta) ;
	}

}
