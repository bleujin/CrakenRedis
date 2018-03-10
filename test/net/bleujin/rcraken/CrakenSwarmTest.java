package net.bleujin.rcraken;

import java.io.File;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.store.MapConfig;

public class CrakenSwarmTest {

	@Test
	public void makeTest() {
		
		Craken craken = CrakenConfig
			.makeSwarm("memory", MapConfig.memory())
			.makeSwarm("file", MapConfig.file(new File("./resource/file.db")))
			.build().start() ;
		
		ReadSession msession = craken.login("memory") ;
		msession.tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("age", 20).merge();
			wsession.pathBy("/emp/hero").property("name", "hero").property("age", 30).merge();
		}) ;

		ReadSession fsession = craken.login("file") ;
		fsession.tran(wsession ->{
			wsession.pathBy("/femp/bleujin").property("name", "bleujin").property("age", 20).merge();
			wsession.pathBy("/femp/hero").property("name", "hero").property("age", 30).merge();
		}) ;

		
		msession.root().walkBreadth().debugPrint();
		fsession.root().walkBreadth().debugPrint();
		
		fsession.workspace().removeSelf() ;
	}
}
