package net.bleujin.rcraken.script;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class ScripterTest {

	@Test
	public void testJScript() throws Exception {
		Scripter je = Scripter.javascript();
		String hello = IOUtil.toStringWithClose(getClass().getResourceAsStream("helloworld.jscript"));
		InstantScript script = je.createScript(IdString.create("hello"), "hello world", new StringReader(hello));
		String result = script.call(new ResultHandler<String>() {
			public String onSuccess(Object result, Object... args) {
				return (String) result;
			}

			public String onFail(Exception ex, Object... args) {
				ex.printStackTrace();
				return null;
			}
		}, "sayHello", "bleujin");

		Debug.line(result, script.compiled().getClass());
	}

	@Test
	public void testGScript() throws Exception {
		Scripter ge = Scripter.groovy();
		String hello = IOUtil.toStringWithClose(getClass().getResourceAsStream("helloworld.groovy"));
		InstantScript script = ge.createScript(IdString.create("hello"), "hello world", new StringReader(hello));
		String result = script.call(new ResultHandler<String>() {
			public String onSuccess(Object result, Object... args) {
				return (String) result;
			}

			public String onFail(Exception ex, Object... args) {
				ex.printStackTrace();
				return null;
			}
		}, "sayHello", "bleujin");
		Debug.line(result, script.compiled().getClass());
	}

}
