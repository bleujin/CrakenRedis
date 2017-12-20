package net.bleujin.plan;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.TemplateNode;
import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.mte.Engine;
import net.ion.framework.mte.Renderer;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;

public class TemplateProperty extends TestBaseCrakenRedis {

	@Test
	void directTransform() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			ReadNode self = rsession.pathBy("/emp");
			
			List<ReadNode> children = self.children().stream().filter(node -> node.property("age").asLong() > 20).collect(Collectors.toList()) ;
			
			Engine engine = rsession.workspace().parseEngine();
//			String result = engine.transform("${foreach children child \n}<tr><td>${child.asString(name)}</td><td>${child.property(age).asLong()}</td></tr>${end}", MapUtil.<String, Object>create("children", children)) ;
			String result = engine.transform("${foreach self.children().stream().filter(node -> node.property(\"age\").asLong() > 20).collect(Collectors.toList()) \n}<tr><td>${child.asString(name)}</td><td>${child.property(age).asLong()}</td></tr>${end}", MapUtil.<String, Object>create("self", self)) ;
			Debug.line(result) ;
		}) ;
	} // i need template engine with lambda expression

	
	@Test
	void trasnslatePath() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			TemplateNode tnode = rsession.templateBy("/emp.children").parameters("pageNo=1&listNum=10&format=json") ;
			tnode.targetNode() ;
			tnode.templateNode() ;
			tnode.template();
			
//			TemplateNode tnode = rsession.templateBy("/emp/bleujin.json").parameters("detail=yes") ;
		}) ;
		
		
	}
}
