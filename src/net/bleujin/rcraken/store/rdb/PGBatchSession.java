package net.bleujin.rcraken.store.rdb;

import net.bleujin.rcraken.BatchNode;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.Fqn;
import net.ion.framework.db.DBController;
import net.ion.framework.parse.gson.JsonObject;

public class PGBatchSession extends BatchSession{

	private PGWriteSession wsession;
	private PGWorkspace workspace;
	private long tranSeq;
	public PGBatchSession(PGWorkspace workspace, PGWriteSession wsession) {
		super(workspace, wsession.readSession()) ;
		this.workspace = workspace ;
		this.wsession = wsession ;
		this.tranSeq = workspace.nextTranSeq() ;
	}

	@Override
	protected void insert(BatchNode wnode, Fqn fqn, JsonObject data) { // duplicate code(batchnode is not writenode)
		// wsession.merge(null, fqn, data);
		
		DBController dc = workspace.dc() ;
		workspace.execUpdate(dc.createUserProcedure("craken@dataWith(?,?,?,?)").addParam(workspace.name()).addParam(fqn.absPath()).addParam(data.toString()).addParam(fqn.getParent().absPath())) ;
		workspace.sessionMerge(this.tranSeq, fqn, data) ;
		
		// no handle lob
	}

}
