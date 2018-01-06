package net.bleujin.rcraken.extend;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.convert.FieldDefinition;
import net.bleujin.rcraken.tbase.TestBaseCrakenRedis;
import net.ion.framework.db.Rows;

public class AdRowsTest extends TestBaseCrakenRedis {

	@Test
	public void ChildrenToRows() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 10L).merge();
			wsession.pathBy("/emp/hero").property("name", "hero").property("age", 20L).merge();
			wsession.pathBy("/emp/jin").property("name", "jin").property("age", 30L).merge();
			
			wsession.pathBy("/dept/dev").property("name", "developer").refTo("dept", "/emp/bleujin", "/emp/hero", "/emp/jin", "/emp/unknown").merge();
			return null;
		}).get();
		
		Rows rows = rsession.pathBy("/emp").children().stream().skip(1).filter(rnode -> rnode.property("age").asLong() >= 30)
				.toRows("name, age", new FieldDefinition("cnt", (fcontext, current) -> {return current.property("age").asInt() * 2 ;})) ;
		rows.debugPrint(); 
		rsession.pathBy("/dept/dev").refs("dept").stream().filter(rnode -> rnode.property("age").asLong() >= 20).toRows("name as myname, age as myage, name + 'd' as cname").debugPrint();
		
	}
	
	@Test
	public void walkChildrenToRows() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").merge();
			wsession.pathBy("/emp/hero").property("name", "hero").merge();
			wsession.pathBy("/emp/jin").property("name", "jin").merge();
			wsession.pathBy("/emp/bleujin/address").property("city", "seoul").property("date", Calendar.getInstance()).merge();
			return null;
		}).get() ;

		rsession.pathBy("/emp").walkBreadth().stream().toRows("name, city").debugPrint();
	}
}
