package net.bleujin.rcraken.extend;

import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.bleujin.rcraken.extend.CRows;
import net.bleujin.rcraken.extend.rows.FieldContext;
import net.bleujin.rcraken.extend.rows.FieldDefinition;
import net.bleujin.rcraken.extend.rows.FieldRender;
import net.ion.framework.db.Page;
import net.ion.framework.db.Rows;

public class TestAdRows extends TestBaseCrakenRedis {

	public void testToRows() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 10).merge();
			wsession.pathBy("/emp/hero").property("name", "hero").property("age", 20).merge();
			wsession.pathBy("/emp/jin").property("name", "jin").property("age", 30).merge(); // loop
			return null;
		});
		
		Rows rows = CRows.create(rsession.pathBy("/emp").children().stream()).page(Page.TEN).toRows("name, age", new FieldDefinition("cnt", (fcontext, current)-> { return 3; })) ;

	}
}
