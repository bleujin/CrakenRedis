package net.bleujin.rcraken.db;

import org.junit.jupiter.api.Test;

import net.ion.framework.db.Rows;
import net.ion.framework.db.procedure.IUserProcedureBatch;

public class UserProcedureBatchTest extends TestBaseFnManager {

	
	@Test
	public void testCreateUserProcedures() throws Exception {
		IUserProcedureBatch upt = dc.createUserProcedureBatch("dummy@batchWith(?,?,?)");
		upt.addParam(new String[]{"bleujin", "hero", "jin"})
			.addParam(new int[]{20, 30, 40})
			.addParam(new String[]{"seoul", "busan", "inchon"}).execUpdate() ;
		
		
		Rows rows = dc.createUserProcedure("dummy@listPersonBy()").execQuery();
		rows.debugPrint();
	}
	

	@Test
	public void testConvertPrimitive() throws Exception {
		IUserProcedureBatch upt = dc.createUserProcedureBatch("dummy@batchWith(?,?,?)");
		upt.addParam(new String[]{"bleujin", "hero", "jin"})
			.addParam(new Integer[]{20, 30, 40})
			.addParam(new String[]{"seoul", "busan", "inchon"}).execUpdate() ;
		
		
		Rows rows = dc.createUserProcedure("dummy@listPersonBy()").execQuery();
		rows.debugPrint();
	}

}
