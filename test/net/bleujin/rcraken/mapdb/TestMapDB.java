package net.bleujin.rcraken.mapdb;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.redis.TestBaseRedis;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.bleujin.searcher.SearchControllerConfig;
import net.ion.framework.mte.Engine;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;

public class TestMapDB extends TestBaseMapDB {

	@Test
	public void initialize() throws Exception {
		rsession.tran(TestBaseRedis.SAMPLE).get();
		rsession.pathBy("/").walkDepth().debugPrint();
	}

	@Test
	public void lobProperty() throws Exception {
		rsession.tran(wsession -> {
			FileInputStream fis = new FileInputStream("./resource/helloworld.txt");
			wsession.pathBy("/bleujin").property("helloworld", fis).merge();
			;
			return null;
		});

		InputStream input = rsession.pathBy("/bleujin").property("helloworld").asStream();
		Debug.line(IOUtil.toStringWithClose(input));
	}

	@Test
	public void indexData() throws Exception {
		rsession.workspace().indexCentral(SearchControllerConfig.newRam().build(OpenMode.CREATE_OR_APPEND));
		rsession.tran(TestBaseRedis.SAMPLE).get();

		rsession.pathBy("/").childQuery("", true).find().debugPrint();
		rsession.workspace().indexCentral(null) ;
	}

	@Test
	public void indexDataWhenNotSet() throws Exception {
		assertThrows(IllegalStateException.class, () -> {
			rsession.tran(TestBaseRedis.SAMPLE).get();
			rsession.pathBy("/").childQuery("", true).find().debugPrint();
		});
	}

	
	@Test
	public void removeChild() throws Exception {
		rsession.tran(TestBaseRCraken.SAMPLE) ;
		rsession.tranSync(wsession -> {
			wsession.pathBy("/emp").removeChild(); 
		}) ;
		
		rsession.pathBy("/emp").children().debugPrint(); 
	}
	

	@Test
	public void directTransform() throws Exception {
		rsession.tran(SAMPLE).thenAccept(nil -> {
			ReadNode self = rsession.pathBy("/emp");
			// self.children().stream().filter(n -> n.property("age").asInt() >
			// 10).debugPrint();

			Engine engine = rsession.workspace().parseEngine();
			Debug.line(engine.transform("${foreach self.children().stream().where(\"this.age > 20\").toList() child \n}${child}${end}", MapUtil.<String, Object>create("self", self)));
			// Debug.line(engine.transform("${foreach self.children().stream().filter(n ->
			// n.property(\"age\").asInt() > 10).toList() child \n}${child}${end}",
			// MapUtil.<String, Object>create("self", self))) ;
		});
	} // i need template engine with lambda expression

}
