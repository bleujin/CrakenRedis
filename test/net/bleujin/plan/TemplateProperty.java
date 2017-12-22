package net.bleujin.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.OutputStreamWriter;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.bleujin.rcraken.template.TemplateNode;
import net.ion.framework.mte.Engine;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;

public class TemplateProperty extends TestBaseCrakenRedis {

	@Test
	void directTransform() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			ReadNode self = rsession.pathBy("/emp");
			
			Engine engine = rsession.workspace().parseEngine();
//			String result = engine.transform("${foreach self.children().stream().filter(node -> node.property(\"age\").asLong() > 20).collect(Collectors.toList()) child \n}<tr><td>${child.asString(name)}</td><td>${child.property(age).asLong()}</td></tr>${end}", MapUtil.<String, Object>create("self", self)) ;
//			Debug.line(result) ;
			
			Debug.line(engine.transform("[${foreach self.children() child ,}${child.toJson()}${end}]", MapUtil.<String, Object>create("self", self))) ;
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
		writer.write("\n");
		rsession.templateBy("/emp/bleujin.json").parameters("detail=yes").transform(writer); ;

		rsession.tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("json", "Hello ${self.asString(name)}, p : ${params.asString(detail)}").merge();
			return null ;
		}) ;
		writer.write("\n");
		rsession.templateBy("/emp/bleujin.json").parameters("detail=yes").transform(writer); ;
		writer.write("\n");
		rsession.templateBy("/emp/hero.json").parameters("detail=yes").transform(writer); ;
		
		writer.flush();
	}
}
