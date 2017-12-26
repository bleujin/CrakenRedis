package net.bleujin.rcraken.script.javascript;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.lang.SystemUtils;

import net.ion.framework.util.IOUtil;

public class ScriptJDK {

	public final static String trans(String source){
		
		if (SystemUtils.JAVA_VM_SPECIFICATION_VERSION.compareTo("1.8") >= 0){
			String packReplace = source.replaceAll("importPackage\\(([^\\)]*)", "importPackage\\('$1'") ;
			
			return "try { load(\"nashorn:mozilla_compat.js\"); } catch(e) {} \r\n" + packReplace ;
		}
		return source ;
	}

	public final static String trans(InputStream source) throws IOException{
		return trans(IOUtil.toStringWithClose(source)) ;
	}
	
	public final static String trans(Reader source) throws IOException{
		return trans(IOUtil.toStringWithClose(source)) ;
	}
	

	
}
