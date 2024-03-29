package net.bleujin.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.OutputStreamWriter;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.WriteNode;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.bleujin.rcraken.template.TemplateNode;
import net.ion.framework.mte.Engine;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;

public class NodeAsTemplate extends TestBaseRCraken {

	@Test
	void directTransform() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			ReadNode self = rsession.pathBy("/emp");
			//self.children().stream().where("age", 10).debugPrint();
			Engine engine = rsession.workspace().parseEngine();

			Debug.line(engine.transform("[${foreach self.children().stream().gte(age,10).toList() child \n}${child.toJson()}${end}]", MapUtil.<String, Object>create("self", self))) ;
		}) ;
	} // i need template engine with lambda expression

	
	@Test
	void trasnslatePath() throws Exception {
		rsession.tran(SAMPLE) ;

		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		TemplateNode tnode = rsession.templateBy("/emp.children").parameters("pageNo=1&listNum=10&format=json") ;
		
		assertEquals("/emp", tnode.targetNode().fqn().absPath()) ;
		assertEquals("children", tnode.templateName()) ;
		
		tnode.transform(writer);
		
		rsession.tran(wsession ->{
			WriteNode bnode = wsession.pathBy("/emp/bleujin");
			bnode.unset("json") ;
			bnode.merge();
		}) ;
		
		rsession.templateBy("/emp/bleujin.json").parameters("detail=yes").transform(writer); ;

		Debug.line() ;
		rsession.tran(wsession ->{
			wsession.pathBy("/emp").property("json", "Hello ${self.asString(name)}, p : ${params.asString(detail)}").merge();
		}) ;
		
		
		rsession.templateBy("/emp/bleujin.json").parameters("detail=yes").transform(writer); ;
		rsession.templateBy("/emp/hero.json").parameters("detail=yes").transform(writer); ;
		
		IOUtil.closeQuietly(writer);
	}
}
