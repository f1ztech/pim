package ru.mipt.pim.server.mail;


public class MailException extends Exception {

	private static final long serialVersionUID = 6627837044680343545L;

	public MailException() {
        super();
    }

    public MailException(String message) {
        super(message);
    }

    public MailException(Throwable cause) {
        super(cause);
    }

    public MailException(String message, Throwable cause) {
        super(message, cause);
    }
}