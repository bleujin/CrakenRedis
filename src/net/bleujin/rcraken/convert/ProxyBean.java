package net.bleujin.rcraken.convert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import net.bleujin.rcraken.ReadNode;

public class ProxyBean implements MethodInterceptor{

	private ReadNode node ;
	private volatile boolean initialized = false ;
	private TypeStrategy ts ;
	public ProxyBean(TypeStrategy ts, ReadNode node) {
		this.ts = ts ;
		this.node = node ;
	}

	public final static <T> T create(TypeStrategy ts, ReadNode node, Class<T> clz){
		try {
			
			Enhancer e = new Enhancer();
			e.setSuperclass(clz);
			e.setCallback(new ProxyBean(ts, node));
			
			return (T)e.create();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}

	}
	
	public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy) throws Throwable {
		synchronized (this) {
			while (! initialized) {
				for(Field field : obj.getClass().getSuperclass().getDeclaredFields()){
					if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue ;
					
					if (ts.supported(field, node)){
						field.setAccessible(true) ;
						ts.resolveAdaptor(obj, field, node) ;
					}
				}
				initialized = true ;
			}
		}
		
		
		return proxy.invokeSuper(obj, args);
	}
	
}

