package net.devstudy.ishop.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class ResourceNotFoundException extends AbstractApplicationException {
	private static final long serialVersionUID = -6121766502027524096L;

	public ResourceNotFoundException(String s) {
		super(s, HttpServletResponse.SC_NOT_FOUND);
	}
}
