package net.bleujin.rcraken;

import net.ion.framework.util.ObjectUtil;

public interface CommonNode  {

	public Property property(String key);
	public String asString(String name) ;
	public Object asValue(String name) ;
	public <T> T defaultValue(String name, T dftValue) ;
	
	public CommonNode child(String fqn);
	
	public boolean hasChild(String fqn);
	public boolean hasProperty(String pid) ;
	public CommonNode parent();
	public boolean hasRef(String refName);
	public CommonNode ref(String refName);
}
