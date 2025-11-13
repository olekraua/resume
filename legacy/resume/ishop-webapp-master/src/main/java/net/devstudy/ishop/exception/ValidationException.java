package net.devstudy.ishop.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class ValidationException extends AbstractApplicationException {
	private static final long serialVersionUID = -6843925636139273536L;

	public ValidationException(String s) {
		super(s, HttpServletResponse.SC_BAD_REQUEST);
	}
}
