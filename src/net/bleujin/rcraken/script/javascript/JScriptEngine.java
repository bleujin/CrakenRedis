package net.bleujin.rcraken.script.javascript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang.SystemUtils;

import net.bleujin.rcraken.script.AbstractListener;
import net.bleujin.rcraken.script.DirClassLoader;
import net.bleujin.rcraken.script.FileAlterationMonitor;
import net.bleujin.rcraken.script.IdString;
import net.bleujin.rcraken.script.OuterClassLoader;
import net.bleujin.rcraken.script.ResultHandler;
import net.bleujin.rcraken.script.Scripter;
import net.bleujin.rcraken.script.StringInputStream;
import net.ion.framework.logging.LogBroker;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.ObjectId;
import net.ion.framework.util.WithinThreadExecutor;

public class JScriptEngine extends Scripter {

	public final static String EntryName = "jsentry";

	private ScriptEngineManager manager;
	private ScriptEngine sengine;
	private ExecutorService es = new WithinThreadExecutor();
	private ClassLoader cloader;
	private Logger log = LogBroker.getLogger(JScriptEngine.class);

	private JScriptEngine(ClassLoader cloader) {
		this.manager = new ScriptEngineManager();
		this.sengine = manager.getEngineByName("Nashorn");
		Bindings bindings = new SimpleBindings();
		bindings.put("dirloader", cloader);
		sengine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
		this.cloader = cloader;
	}

	public static JScriptEngine create() {
		return new JScriptEngine(Thread.currentThread().getContextClassLoader());
	}

	public static JScriptEngine create(String libPath) throws Exception {
		return create(libPath, null, false);
	}

	public static JScriptEngine create(String libPath, ScheduledExecutorService ses, boolean reload) throws Exception {
		final File libDir = new File(libPath);
		final Logger l = LogBroker.getLogger(JScriptEngine.class);
		if (libDir.exists() && libDir.isDirectory()) {
			ClassLoader cloader = new DirClassLoader(libPath);
			if (reload) {
				final OuterClassLoader classloader = new OuterClassLoader(cloader);
				FileAlterationObserver fo = new FileAlterationObserver(libDir);
				fo.addListener(new AbstractListener() {
					@Override
					public void onFileChange(File file) {
						try {
							classloader.change(new DirClassLoader(libDir));
						} catch (IOException ignore) {
							ignore.printStackTrace();
						}
					}
				});
				FileAlterationMonitor fam = new FileAlterationMonitor(3000, ses, fo);
				fam.start();
			}
			return new JScriptEngine(cloader);
		} else {
			l.info("Has not Extension libPath : " + libPath);
			return create();
		}
	}

	ClassLoader cloader() {
		return cloader;
	}

	public JScriptEngine executorService(ExecutorService es) {
		this.es = es;
		return this;
	}

	public ExecutorService executor() {
		return es;
	}

	public InstantJavaScript testScript(String script) throws IOException, ScriptException {
		return createScript(IdString.create(new ObjectId().toString()), "", new StringInputStream(script));
	}

	public InstantJavaScript createScript(IdString lid, String explain, InputStream input) throws IOException, ScriptException {
		return createScript(lid, explain, new InputStreamReader(input));
	}

	public InstantJavaScript createScript(IdString lid, String explain, Reader reader) throws IOException, ScriptException {
		try {
			String script = ScriptJDK.trans(reader);

			Object compiledScript = sengine.eval(script);
			InstantJavaScript result = InstantJavaScript.create(this, explain, compiledScript);

			return result;
		} finally {
			IOUtil.close(reader);
		}
	}

	<T> Future<T> runAsyncHandle(final InstantJavaScript script, final ResultHandler<T> rhandler, final Object... args) {
		return es.submit(new Callable<T>() {
			@Override
			public T call() {
				try {
					Object result = ((Invocable) sengine).invokeMethod(script.compiled(), "handle", args);
					return rhandler.onSuccess(result, args);
				} catch (ScriptException e) {
					return rhandler.onFail(e, args);
				} catch (NoSuchMethodException e) {
					return rhandler.onFail(e, args);
				} catch (Exception e) {
					return rhandler.onFail(e, args);
				}
			}
		});
	}

	<T> T execHandle(final InstantJavaScript script, ResultHandler<T> rhandler, String method, Object... args) {
		try {
			Object result = ((Invocable) sengine).invokeMethod(script.compiled(), method, args);
			return rhandler.onSuccess(result, args);
		} catch (ScriptException e) {
			return rhandler.onFail(e, args);
		} catch (NoSuchMethodException e) {
			return rhandler.onFail(e, args);
		} catch (Exception e) {
			e.printStackTrace();
			return rhandler.onFail(e, args);
		}

	}

	public boolean hasMethod(final InstantJavaScript script, String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (SystemUtils.JAVA_VM_SPECIFICATION_VERSION.compareTo("1.8") >= 0) {
			return (Boolean) MethodUtils.invokeMethod(script.compiled(), "hasMember", new Object[] { methodName });
		}
		Object result = MethodUtils.invokeMethod(script.compiled(), "getAllIds", new Object[0]);
		Object[] names = (Object[]) result;
		return ArrayUtil.contains(names, methodName);
	}
}
