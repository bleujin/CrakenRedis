package net.bleujin.rcraken.script;

public interface ExceptionHandler {

	public final static ExceptionHandler DEFAULT = new ExceptionHandler() {
		@Override
		public Object handle(Exception ex) {
			ex.printStackTrace(); 
			return ex;
		}
	};

	public Object handle(Exception ex) ;
}
