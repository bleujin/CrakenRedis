package net.bleujin.rcraken.db;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import net.bleujin.rcraken.ReadSession;
import net.ion.framework.db.Rows;

public class CrakenScriptManager extends CrakenManager {

	private final CrakenScript cs;
	private CrakenScriptManager(CrakenScript cs) {
		this.cs = cs ;
	}

	public static CrakenScriptManager create(ReadSession rsession, ScheduledExecutorService ses, File baseScriptDir) throws IOException {
		CrakenScript cs = new CrakenScript(rsession, ses) ;
		cs.readDir(baseScriptDir) ;
		
		return new CrakenScriptManager(cs) ;
	}

	@Override
	public Rows queryBy(CrakenUserProcedure cupt) throws Exception {
		return cs.execQuery(cupt.getProcName(), cupt.getParams().toArray(new Object[0])) ;
	}

	@Override
	public int updateWith(CrakenUserProcedure cupt) throws Exception {
		return cs.execUpdate(cupt.getProcName(), cupt.getParams().toArray(new Object[0])) ;
	}

	@Override
	public int updateWith(CrakenUserProcedureBatch cupt) throws Exception {
		return cs.execUpdate(cupt.getProcName(), cupt.getParams().toArray(new Object[0])) ;
	}

	@Override
	public int updateWith(CrakenUserProcedures cupts) throws Exception {
		throw new UnsupportedOperationException("if you must do it, call bleujin") ;
	}

}
