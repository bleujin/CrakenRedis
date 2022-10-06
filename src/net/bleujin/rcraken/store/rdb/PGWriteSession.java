package net.bleujin.rcraken.store.rdb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteNode;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.Property.PType;
import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.ListUtil;

public class PGWriteSession extends WriteSession {

	
	private PGWorkspace workspace;
	private DBController dc;
	private PGReadSession rsession;
	private final long tranSeq ;
	
	public PGWriteSession(PGWorkspace workspace, ReadSession rsession) {
		super(workspace, rsession) ;
		this.workspace = workspace ;
		this.dc = workspace.dc() ;
		this.rsession = (PGReadSession) rsession ;
		this.tranSeq = workspace.nextTranSeq() ;
	}

	@Override
	protected void merge(WriteNode wnode, Fqn fqn, JsonObject data) {
		workspace.execUpdate(dc.createUserProcedure("craken@dataWith(?,?,?,?)").addParam(workspace.name()).addParam(fqn.absPath()).addParam(data.toString()).addParam(fqn.getParent().absPath())) ;
		workspace.sessionMerge(this.tranSeq, fqn, data) ;
		
		// handle lob
		wnode.properties().filter(p -> PType.Lob.equals(p.type())&& hasAttribute(p.asString())).forEach(p ->{
			if (attrs().get(p.asString()) instanceof InputStream) {
	        	InputStream input = (InputStream) attrs().get(p.asString()) ;
	        	String targetDir = fqn.getParent().isRoot() ? "" : fqn.getParent().absPath() ;
	        	File dir = new File(workspace.workspaceRootDir(), targetDir) ;
	        	if (!dir.exists()) dir.mkdirs() ;
	        	File targetFile = new File(dir, fqn.name() + "." + p.name()) ; // rootDir/wname/fqn's parent/nodename.pname
	        	try {
					IOUtil.copyNCloseSilent(input, new FileOutputStream(targetFile)) ;
				} catch (IOException ex) {
					attrs().put(p.name(), ex.getMessage()) ;
					throw new IllegalStateException(ex) ;
				}
			}
		});
	}

	@Override
	protected void removeChild(WriteNode wnode, Fqn fqn, JsonObject data) {
		try {
			Rows rows = workspace.execQuery(dc.createUserProcedure("craken@childDataBy(?,?)").addParam(workspace.name()).addParam(fqn.absPath())) ;
			while(rows.next()) {
				String absFqn = rows.getString("fqn") ;
				String jdata = rows.getString("jdata") ;
				workspace.sessionRemove(tranSeq, fqn, JsonObject.fromString(jdata));
			}
		} catch(SQLException ignore) {
			ignore.printStackTrace(); 
		}
		
		workspace.execUpdate(dc.createUserProcedure("craken@removeChildWith(?,?)").addParam(workspace.name()).addParam(fqn.absPath())) ;
	}

	@Override
	protected void removeSelf(WriteNode wnode, Fqn fqn, JsonObject data) {
		workspace.execUpdate(dc.createUserProcedure("craken@removeSelfWith(?,?)").addParam(workspace.name()).addParam(fqn.absPath())) ;
		workspace.sessionRemove(this.tranSeq, fqn, data) ;
	}

	@Override
	protected Set<String> readStruBy(Fqn fqn) {
		return rsession.readStruBy(fqn) ;
	}

	@Override
	protected JsonObject readDataBy(Fqn fqn) {
		JsonObject data = rsession.readDataBy(fqn) ;
		workspace.sessionRead(this.tranSeq, fqn, data) ;
		return data ;
	}

	@Override
	public boolean exist(String fqn) {
		return rsession.exist(fqn) ;
	}
	
	public void endTran() {
		super.endTran(); 
		workspace.sessionEnd(this.tranSeq) ;
	}


}
