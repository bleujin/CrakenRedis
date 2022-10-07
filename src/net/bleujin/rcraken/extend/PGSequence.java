package net.bleujin.rcraken.extend;

import java.sql.SQLException;

import net.ion.framework.db.DBController;

public class PGSequence implements Sequence {

	private DBController dc;
	private String name;

	public PGSequence(DBController dc, String name) {
		this.dc = dc ;
		this.name = name ;
		execDDL("CREATE SEQUENCE IF NOT EXISTS public." + fullName()) ;
	}
	
	private String fullName() {
		return name + "_seq" ;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public long get() {
		return execSeqQuery("select currval('" + fullName() +"')");
	}

	@Override
	public void set(long newValue) {
		execSeqQuery("select setval('" + fullName() +"', " + newValue + ", true)");
	}

	@Override
	public long incrementAndGet() {
		return execSeqQuery("select nextval('" + fullName() +"')");
	}
	@Override
	public long getAndIncrement() {
		return incrementAndGet() - 1;
	}

	@Override
	public void delete() {
		execDDL("drop SEQUENCE " + fullName());
	}


	@Override
	public long decrementAndGet() {
		throw new UnsupportedOperationException("cause concurrent problem") ;
	}

	@Override
	public long getAndDecrement() {
		throw new UnsupportedOperationException("cause concurrent problem") ;
	}

	@Override
	public long addAndGet(long delta) {
		throw new UnsupportedOperationException("cause concurrent problem") ;
	}

	@Override
	public long getAndAdd(long delta) {
		throw new UnsupportedOperationException("cause concurrent problem") ;
	}

	
	private void execDDL(String query) {
		try {
			dc.execUpdate(query) ;
		} catch (SQLException ex) {
			throw new IllegalStateException(ex) ;
		}
	}

	private long execSeqQuery(String query) {
		return dc.execQuery(query).firstRow().getInt(1);
	}


}
