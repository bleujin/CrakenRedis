package net.bleujin.rcraken.db;

import java.io.File;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.db.CrakenScriptManager;
import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.procedure.IUserProcedureBatch;
import net.ion.framework.db.servant.StdOutServant;
import net.ion.framework.util.Debug;

public class TestCrakenScriptManager extends TestCase {

	protected DBController dc;
	private Craken craken;
	protected ReadSession session;

	@Override
	protected void setUp() throws Exception {
		this.craken = CrakenConfig.redisSingle().build() ;
		this.session = craken.start().login("test") ;
		session.workspace().removeSelf() ;
		
		CrakenScriptManager dbm = CrakenScriptManager.create(session, Executors.newScheduledThreadPool(1), new File("./test/net/bleujin/rcraken/db")) ;
		this.dc = new DBController("craken", dbm, new StdOutServant());
		dc.initSelf() ;
	}
	
	@Override
	protected void tearDown() throws Exception {
		dc.destroySelf();
		craken.shutdownSelf();
	}
	
	
	public void testCreateUserProcedure() throws Exception {
		int result = dc.createUserProcedure("afield@createWith(?,?)").addParam("rday").addParam("registerDay").execUpdate() ;
		Debug.line(result);
		
		Rows rows = dc.createUserProcedure("afield@listBy(?,?)").addParam(0).addParam(2).execQuery() ;
		rows.debugPrint(); 
		rows.first() ;
		assertEquals("rday", rows.getString("afieldId"));
	}
	
	public void testCreateUserProcedureBatch() throws Exception {
		IUserProcedureBatch bat = dc.createUserProcedureBatch("afield@batchWith(?,?)") ;
		bat.addBatchParam(0, "rday") ;
		bat.addBatchParam(0, "registerday");
		
		bat.addBatchParam(1, "cday") ;
		bat.addBatchParam(1, "createday");
		int result = bat.execUpdate() ;
		
		assertEquals(2, result);
		Rows rows = dc.createUserProcedure("afield@listBy(?,?)").addParam(1).addParam(2).execQuery() ;
		rows.debugPrint(); 
	}
	
}
