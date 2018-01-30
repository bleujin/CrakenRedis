package net.bleujin.rcraken.extend;

import java.util.Map;

import net.bleujin.rcraken.WriteJobNoReturn;

public abstract class ModifyCDDHandler implements CDDHandler {

	
	private String pattern;
	public ModifyCDDHandler(String pattern) {
		this.pattern = pattern ;
	}

	@Override
	public String id() {
		return pattern;
	}

	@Override
	public String pathPattern() {
		return pattern;
	}

	public abstract WriteJobNoReturn modified(Map<String, String> resolveMap, CDDModifiedEvent event) ;

	@Override
	public WriteJobNoReturn deleted(Map<String, String> resolveMap, CDDRemovedEvent event) {
		return null;
	}

}
