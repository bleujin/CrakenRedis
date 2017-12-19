package net.bleujin.rcraken;

public interface BatchJob<T> {
	
	public T handle(BatchSession bsession) throws Exception;
}
