package net.bleujin.rcraken.db;

import net.bleujin.rcraken.ReadSession;

public abstract class QueryPackage {

	private ReadSession session ;
	
	protected ReadSession session() {
		if (session == null) throw new UnsupportedOperationException();
		return session ;
	}
	
}
