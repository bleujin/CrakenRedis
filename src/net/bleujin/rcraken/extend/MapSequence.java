package net.bleujin.rcraken.extend;

import org.mapdb.Atomic.Long;

public class MapSequence  implements Sequence{

	private Long value;
	private String name;

	MapSequence(Long value, String name) {
		this.value = value;
		this.name = name;
		
	}

	public static MapSequence create(Long value, String name) {
		return new MapSequence(value, name);
	}

	public String name() {
		return name;
	}

	public long get() {
		return value.get();
	}

	public void set(long newValue) {
		value.set(newValue);
	}

	public long incrementAndGet() {
		return value.incrementAndGet();
	}

	public long decrementAndGet() {
		return value.decrementAndGet();
	}

	public long getAndIncrement() {
		return value.getAndIncrement();
	}

	public long getAndDecrement() {
		return value.getAndDecrement();
	}

	public void delete() {
		set(0L);
	}

	public long addAndGet(long delta) {
		return value.addAndGet(delta);
	}

	public long getAndAdd(long delta) {
		return value.getAndAdd(delta);
	}

}
