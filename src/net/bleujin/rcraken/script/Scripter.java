package net.bleujin.rcraken.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.script.ScriptException;

import net.bleujin.rcraken.script.groovy.GScriptEngine;
import net.bleujin.rcraken.script.javascript.JScriptEngine;

public abstract class Scripter {
	
	public abstract <T extends InstantScript> T createScript(IdString lid, String explain, InputStream input) throws IOException, ScriptException ;
	public abstract <T extends InstantScript> T createScript(IdString lid, String explain, Reader reader) throws IOException, ScriptException ;
	
	public static JScriptEngine javascript() {
		return JScriptEngine.create();
	}
	public static GScriptEngine groovy() {
		return GScriptEngine.create() ;
	}
}
