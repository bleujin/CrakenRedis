package net.bleujin.rcraken;

public interface WriteJob<T> {

	public T handle(WriteSession wsession) throws Exception;
}
