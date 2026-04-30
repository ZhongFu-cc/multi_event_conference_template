package tw.com.conference.exception;

public class OrderPaymentException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public OrderPaymentException(String message) {
        super(message);
    }
	
}
