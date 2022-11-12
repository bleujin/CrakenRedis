package net.bleujin.rcraken.mapdb;

import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import net.ion.framework.util.Debug;

public class TestMapDBFile {

	@Test
	public void testSpeed() throws Exception {
		DB db = DBMaker.fileDB("./resource/mapdb/file.db").make() ;
		HTreeMap<String, String> map = db.hashMap("stest", Serializer.STRING, Serializer.STRING).makeOrGet() ;

		
		for (int loop = 0; loop < 10; loop++) {
			long start = System.currentTimeMillis() ;
			for (int i = 0; i < 5; i++) {
				map.put(i + "", i+ "") ;
			}
			db.commit();
			Debug.line(System.currentTimeMillis() - start);
		}
		
	}
	
}
