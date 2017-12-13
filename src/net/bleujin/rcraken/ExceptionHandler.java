package net.bleujin.rcraken;

public interface ExceptionHandler {

	public final static ExceptionHandler PRINT = new ExceptionHandler() {
		public void handle(WriteSession tsession, WriteJob tjob, Throwable ex) {
			ex.printStackTrace();
		}
		
		public void handle(BatchSession tsession, BatchJob tjob, Throwable ex) {
			ex.printStackTrace();
		}
	};

	public void handle(WriteSession tsession, WriteJob tjob, Throwable ex);
	public void handle(BatchSession tsession, BatchJob tjob, Throwable ex);
}
