package net.bleujin.rcraken;

public interface TransactionJob<T> {

	public T handle(WriteSession wsession) throws Exception ;
}
