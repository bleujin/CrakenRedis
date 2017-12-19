package net.bleujin.plan;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.mte.Engine;
import net.ion.framework.mte.Renderer;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;

public class TemplateProperty extends TestBaseCrakenRedis {

	@Test
	void testDirect() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			ReadNode found = rsession.pathBy("/emp");
			
			List<ReadNode> children = found.children().stream().filter(node -> node.property("age").asLong() > 20).collect(Collectors.toList()) ;
			
			Engine engine = rsession.workspace().parseEngine();
			String result = engine.transform("${foreach children child ,}${child.asString(name)}:${child.property(age).asLong()}${end}", MapUtil.<String, Object>create("children", children)) ;
			Debug.line(result) ;
		}) ;
	} // i need template engine with lambda expression

}
