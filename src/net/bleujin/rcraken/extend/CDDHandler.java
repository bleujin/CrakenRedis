package net.bleujin.rcraken.extend;

import java.util.Map;

import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteJobNoReturn;


public interface CDDHandler {

	String id();
	public String pathPattern() ;
	public WriteJobNoReturn modified(Map<String, String> resolveMap, CDDModifiedEvent event) ;
	public WriteJobNoReturn deleted(Map<String, String> resolveMap, CDDRemovedEvent event) ;

}
