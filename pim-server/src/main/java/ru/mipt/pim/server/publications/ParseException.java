package ru.mipt.pim.server.publications;

public class ParseException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6978908674396003047L;

	public ParseException() {
        super();
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}