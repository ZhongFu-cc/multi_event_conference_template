package tw.com.conference.exception;

public class PricingRuleException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PricingRuleException(String message) {
		super(message);
	}

}
