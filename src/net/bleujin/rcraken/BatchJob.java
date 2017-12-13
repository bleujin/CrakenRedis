package net.bleujin.rcraken;

public interface BatchJob {
	
	public void handle(BatchSession bsession) throws Exception;
}
