package net.devstudy.ishop.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class AccessDeniedException extends AbstractApplicationException {
	private static final long serialVersionUID = -8981777225188967376L;

	public AccessDeniedException(String s) {
		super(s, HttpServletResponse.SC_FORBIDDEN);
	}
}
