package net.devstudy.ishop.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public final class WebUtils {
	public static Cookie findCookie(HttpServletRequest req, String cookieName) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(cookieName)) {
					if (c.getValue() != null && !"".equals(c.getValue())) {
						return c;
					}
				}
			}
		}
		return null;
	}

	public static void setCookie(String name, String value, int age, HttpServletResponse resp) {
		Cookie c = new Cookie(name, value);
		c.setMaxAge(age);
		c.setPath("/");
		c.setHttpOnly(true);
		resp.addCookie(c);
	}
	
	public static String getCurrentRequestUrl(HttpServletRequest req) {
		String query = req.getQueryString();
		if (query == null) {
			return req.getRequestURI();
		} else {
			return req.getRequestURI() + "?" + query;
		}
	}

	private WebUtils() {
	}
}
