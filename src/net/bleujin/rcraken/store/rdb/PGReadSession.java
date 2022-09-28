package net.bleujin.rcraken.store.rdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.Workspace;
import net.ion.framework.db.DBController;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;

public class PGReadSession extends ReadSession{

	private PGWorkspace workspace;
	private DBController dc;

	protected PGReadSession(PGWorkspace wspace) {
		super(wspace);
		this.workspace = wspace ;
		this.dc = wspace.dc() ;
	}

	@Override
	protected Set<String> readStruBy(Fqn fqn) {
		
		Set<String> set = new TreeSet<String>() ;
		workspace.execQuery(dc.createUserProcedure("craken@struBy(?,?)").addParam(workspace.name()).addParam(fqn.absPath()), new ResultSetHandler<Void>() {
			@Override
			public Void handle(ResultSet rs) throws SQLException {
				while(rs.next()) {
					Fqn current = Fqn.from(rs.getString("fqn"));
					set.add(current.getSubFqn(fqn.size(), current.size()).absPath().substring(1) ) ;
					
				}
				return null ;
			}
		}) ;
		
		return set;
	}

	@Override
	protected JsonObject readDataBy(Fqn fqn) {
		String jdata = workspace.execQuery(dc.createUserProcedure("craken@dataBy(?,?)").addParam(workspace.name()).addParam(fqn.absPath())).firstRow().getString("jdata") ;
		return JsonObject.fromString(jdata) ;
	}

	@Override
	public boolean exist(String fqn) {
		try {
			if ("/".equals(fqn)) return true ;
			return workspace.execQuery(dc.createUserProcedure("craken@existBy(?,?)").addParam(workspace.name()).addParam(fqn)).next();
		} catch (SQLException ex) {
			return false ;
		}
	}

}
