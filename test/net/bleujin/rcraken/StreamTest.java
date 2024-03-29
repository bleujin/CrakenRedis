package net.bleujin.rcraken;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.tbase.TestBaseRCraken;

public class StreamTest extends TestBaseRCraken {

	@Test
	public void testChildren() throws Exception {
		rsession.tran(SAMPLE) ;

		rsession.root().walkBreadth().stream().gte("age", 20).eq("name", "bleujin").debugPrint();
		
		rsession.root().walkBreadth().stream().and(node -> node.property("age").asInt() >= 20, node -> node.property("name").asString().equals("bleujin")).debugPrint();
		
		
//		String moduleName = String.class.getModule().getName();
//		System.out.println("Module name: " + moduleName);
		
		rsession.root().walkBreadth().stream().where("this.age >= 20 and this.name = 'bleujin'").debugPrint();
	}
	
	@Test
	public void childrenSort() throws Exception {
		rsession.tran(SAMPLE) ;
//		rsession.root().walkBreadth().stream().sorted().debugPrint(); 
		
		rsession.root().walkBreadth().stream().hasProperty("age", "name").ascending("age").descending("name").debugPrint();
		
	}
	
	@Test
	public void walkMaxLevel() throws Exception {
		rsession.tran(SAMPLE) ;
		rsession.root().walkDepth(true, 1).stream().debugPrint(); 
		rsession.root().walkDepth(false, 2).stream().debugPrint();

		rsession.root().walkBreadth(true, 1).stream().debugPrint(); 
		rsession.root().walkBreadth(false, 2).stream().debugPrint();
	}

}
