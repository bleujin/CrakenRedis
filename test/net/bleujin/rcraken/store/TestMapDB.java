package net.bleujin.rcraken.store;

import org.junit.jupiter.api.Test;

public class TestMapDB extends TestBaseMapDB{

	
	@Test
	public void initialize() throws Exception{
		rsession.pathBy("/").children().debugPrint(); 
	}
	
	
}
