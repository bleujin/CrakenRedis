package net.bleujin.rcraken.extend;

public interface Sequence {

	public String name();

	public long get();

	public void set(long newValue);

	public long incrementAndGet();

	public long decrementAndGet();

	public long getAndIncrement();

	public long getAndDecrement();

	public void delete();

	public long addAndGet(long delta);

	public long getAndAdd(long delta);

}
