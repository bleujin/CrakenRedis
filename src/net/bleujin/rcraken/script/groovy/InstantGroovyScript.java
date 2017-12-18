package net.bleujin.rcraken.script.groovy;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

import net.bleujin.rcraken.script.InstantScript;
import net.bleujin.rcraken.script.ResultHandler;

public class InstantGroovyScript implements InstantScript {

	private GScriptEngine gengine;
	private String explain;
	private Object compiledScript;

	private InstantGroovyScript(GScriptEngine gengine, String explain, Object compiledScript) {
		this.gengine = gengine ;
		this.explain = explain ;
		this.compiledScript = compiledScript ;
	}

	public static InstantGroovyScript create(GScriptEngine gengine, String explain, Object compiledScript) {
		return new InstantGroovyScript(gengine, explain, compiledScript);
	}

	public Object compiled() {
		return compiledScript;
	}
	
	public <T> T exec(ResultHandler<T> rhandler, Object... args) {
		return call(rhandler, "handle", args) ;
	}
	
	public <T> T call(ResultHandler<T> rhandler, String method, Object... args) {
		return gengine.execHandle(this, rhandler, method, args) ;
	}

	public <T> Future<T> execAsync(ResultHandler<T> rhandler, Object... args) {
		return gengine.runAsyncHandle(this, rhandler, args);
	}

	public boolean hasMethod(String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return gengine.hasMethod(this, methodName);
	}

}
