package net.bleujin.rcraken.store.rdb;

import net.bleujin.rcraken.extend.Sequence;
import net.ion.framework.db.DBController;

public class PGSequence implements Sequence {

	private DBController dc;
	private String name;

	public PGSequence(DBController dc, String name) {
		this.dc = dc ;
		this.name = name ;
	}

	@Override
	public String name() {
		return name;
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
