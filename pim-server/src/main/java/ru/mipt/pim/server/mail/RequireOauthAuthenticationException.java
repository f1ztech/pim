package ru.mipt.pim.server.mail;

public class RequireOauthAuthenticationException extends MailException {

	private static final long serialVersionUID = 6627837044680343545L;

	public RequireOauthAuthenticationException() {
        super();
    }

    public RequireOauthAuthenticationException(String message) {
        super(message);
    }

    public RequireOauthAuthenticationException(Throwable cause) {
        super(cause);
    }

    public RequireOauthAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
