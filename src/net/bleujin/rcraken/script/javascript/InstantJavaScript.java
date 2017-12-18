package net.bleujin.rcraken.script.javascript;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

import net.bleujin.rcraken.script.InstantScript;
import net.bleujin.rcraken.script.ResultHandler;

public class InstantJavaScript implements InstantScript {

	private JScriptEngine app;
	private String explain;
	private Object compiledScript ;
	InstantJavaScript(JScriptEngine app, String explain, Object compiledScript) {
		this.app = app ;
		this.explain = explain ;
		this.compiledScript = compiledScript ;
	}
	
	public static InstantJavaScript create(JScriptEngine app, String explain, Object compiledScript) {
		return new InstantJavaScript(app, explain, compiledScript);
	}

	public Object compiled() {
		return compiledScript;
	}
	
	public <T> T exec(ResultHandler<T> rhandler, Object... args) {
		return call(rhandler, "handle", args) ;
	}
	
	public <T> T call(ResultHandler<T> rhandler, String method, Object... args) {
		return app.execHandle(this, rhandler, method, args) ;
	}

	public <T> Future<T> execAsync(ResultHandler<T> rhandler, Object... args) {
		return app.runAsyncHandle(this, rhandler, args);
	}

	public boolean hasMethod(String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return app.hasMethod(this, methodName);
	}

}
