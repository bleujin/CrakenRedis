package net.bleujin.rcraken.script;

import java.io.InputStream;
import java.net.URL;

public class OuterClassLoader extends ClassLoader{

	private ClassLoader delegate;
	public OuterClassLoader(ClassLoader inner) {
		this.delegate = inner ; 
	}

	
	public void change(ClassLoader delegate){
		this.delegate = delegate ;
	}
	
    @Override
    public void clearAssertionStatus() {
        delegate.clearAssertionStatus();
    }
    @Override
    public URL getResource(String name) {
        return delegate.getResource(name);
    }
    @Override
    public InputStream getResourceAsStream(String name) {
        return delegate.getResourceAsStream(name);
    }
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return delegate.loadClass(name);
    }
    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        delegate.setClassAssertionStatus(className, enabled);
    }
    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        delegate.setDefaultAssertionStatus(enabled);
    }
    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        delegate.setPackageAssertionStatus(packageName, enabled);
    }
}
