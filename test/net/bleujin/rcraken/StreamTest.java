package net.bleujin.rcraken;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.store.TestBaseMapDB;
import net.ion.framework.util.Debug;

public class StreamTest extends TestBaseMapDB {

	@Test
	public void testChildren() throws Exception {
		rsession.tran(SAMPLE) ;

		rsession.root().walkBreadth().stream().and(node -> node.property("age").asInt() >= 20, node -> node.property("name").asString().equals("bleujin")).debugPrint();
		
		rsession.root().walkBreadth().stream().where("this.age >= 20 and this.name = 'bleujin'").debugPrint();
	}
}
