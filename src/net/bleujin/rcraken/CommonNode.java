package net.bleujin.rcraken;

public interface CommonNode  {

	public Property property(String key);
	public ReadNode child(String fqn);
	
	public boolean hasChild(String fqn);
	public boolean hasProperty(String pid) ;
	public ReadNode parent();
	public boolean hasRef(String refName);
	public ReadNode ref(String refName);
}
