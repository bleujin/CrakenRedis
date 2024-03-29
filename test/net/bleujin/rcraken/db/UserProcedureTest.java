package net.bleujin.rcraken.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.ion.framework.db.Rows;

public class UserProcedureTest extends TestBaseFnManager{

	@Test
	public void testFirstStep() throws Exception {
		
//		
		dc.createUserProcedure("dummy@addPersonWITH(?,?,?)").addParam("bleujin").addParam(20).addParam("seoul").execUpdate() ;
		dc.createUserProcedure("dummy@addPersonWITH(?,?,?)").addParam("hero").addParam(20).addParam("busan").execUpdate() ;
		
		Rows rows = dc.createUserProcedure("dummy@findPersonBy(?)").addParam("hero").execQuery() ;
		assertEquals("hero", rows.firstRow().getString("name")) ;
		assertEquals(20, rows.firstRow().getInt("age")) ;
		
		rows.beforeFirst();
		rows.debugPrint();
	}
}
