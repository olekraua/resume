package net.devstudy.ishop.model;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class SocialAccount {
	private final String name;
	private final String email;

	public SocialAccount(String name, String email) {
		super();
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}
}
