package tw.com.conference.exception;

public class FormException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public FormException(String message) {
        super(message);
    }

}
