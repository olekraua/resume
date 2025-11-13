package net.devstudy.ishop.service;

import net.devstudy.ishop.model.SocialAccount;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public interface SocialService {

	String getAuthorizeUrl();

	SocialAccount getSocialAccount(String authToken);
}
