package net.bleujin.rcraken.script;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

public interface InstantScript {

	public Object compiled();

	public <T> T exec(ResultHandler<T> rhandler, Object... args);

	public <T> T call(ResultHandler<T> rhandler, String method, Object... args);

	public <T> Future<T> execAsync(ResultHandler<T> rhandler, Object... args);

	public boolean hasMethod(String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException;

}
