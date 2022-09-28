package net.bleujin.rcraken.store.rdb;

import net.bleujin.rcraken.BatchNode;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.parse.gson.JsonObject;

public class PGBatchSession extends BatchSession{

	private PGWriteSession wsession;
	public PGBatchSession(PGWorkspace workspace, PGWriteSession wsession) {
		super(workspace, wsession.readSession()) ;
		this.wsession = wsession ;
	}

	@Override
	protected void insert(BatchNode wnode, Fqn fqn, JsonObject data) {
		wsession.merge(null, fqn, data);
	}

}
