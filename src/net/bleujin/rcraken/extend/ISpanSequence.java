package net.bleujin.rcraken.extend;

import org.infinispan.Cache;


public class ISpanSequence implements Sequence {

	public ISpanSequence(String seqName, Cache<String, String> cache) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long get() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void set(long newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public long incrementAndGet() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long decrementAndGet() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getAndIncrement() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getAndDecrement() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub

	}

	@Override
	public long addAndGet(long delta) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getAndAdd(long delta) {
		// TODO Auto-generated method stub
		return 0;
	}

}
