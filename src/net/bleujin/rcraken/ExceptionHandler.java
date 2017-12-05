package net.bleujin.rcraken;


public interface ExceptionHandler {

	public final static ExceptionHandler PRINT = new ExceptionHandler(){
		@Override
		public void handle(WriteSession tsession, TransactionJob tjob, Throwable ex) {
			ex.printStackTrace() ;
		}

	} ;
		
	public void handle(WriteSession tsession, TransactionJob tjob, Throwable ex) ;
}
