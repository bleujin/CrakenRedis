package net.bleujin.rcraken.db;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteNode;
import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.servant.StdOutServant;

public class TestBaseFnManager {

	static Craken craken;
	
	protected DBController dc;
	protected ReadSession session;

	@BeforeAll
	static void init() {
		craken = CrakenConfig.redisSingle().build().start() ;
	}
	
	@AfterAll
	static void done() {
		craken.shutdown();
	}
	
	@BeforeEach
	protected void setUp() throws Exception {
		this.session = craken.login("test") ;

		CrakenFnManager dbm = registerFunction() ;
		this.dc = new DBController("craken", dbm, new StdOutServant());
		dc.initSelf() ;
	}
	
	@AfterEach
	protected void tearDown() throws Exception {
		dc.destroySelf();
	}

	private CrakenFnManager registerFunction() {
		CrakenFnManager dbm = new CrakenFnManager(this.craken, "test") ;
		
		dbm.register("dummy", new QueryPackage(){
			
			@Function("addPersonWith")
			public int addPerson(final String name, final int age, final String address) throws Exception{
				return session().tran(wsession -> {
						wsession.pathBy("/persons/" + name).property("name", name).property("age", age).property("address", address).merge(); ;
						return 1 ;
				}).get() ;
			}
			
			public int batchWith(final String[] names, final int[] ages, final String[] address) throws Exception{
				return session().tran(wsession -> {
						WriteNode persons = wsession.pathBy("/persons") ;
						for (int i =0 ; i < names.length ; i++) {
							persons.child(names[i]).property("name", names[i]).property("age", ages[i]).property("address", address[i]).merge(); ;
						}
						return 1 ;
				}).get() ;
			}
			
			public Rows findPersonBy(String name) throws SQLException{
				return session().pathBy("/persons").child(name).toRows("name, age");
			}
			
			public Rows listPersonBy() throws SQLException{
				return session().pathBy("/persons").children().stream().toRows("name, age");
			}
			
			public String toString(){
				return "dummy package" ;
			}
		}) ;
		return dbm ;
	}

}
