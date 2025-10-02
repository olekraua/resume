package net.devstudy.resume.domain;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

/**
 * MongoDB document for Spring Security persistent remember-me token.
 */
@Document(collection = "rememberMeToken")
    // унікальний індекс по 'series'
    @CompoundIndex(name = "series_unique_idx", def = "{ 'series': 1 }", unique = true)
    // неунікальний індекс по 'username'
    @CompoundIndex(name = "username_idx", def = "{ 'username': 1 }")
public class RememberMeToken extends PersistentRememberMeToken {

    @Id
    private String id;

    public RememberMeToken(String id,
                           String username,
                           String series,
                           String tokenValue,
                           Date date) {
        super(username, series, tokenValue, date);
        this.id = id;
    }

    public RememberMeToken(PersistentRememberMeToken token) {
        super(token.getUsername(), token.getSeries(), token.getTokenValue(), token.getDate());
        this.id = null;
    }

    public String getId() {
        return id;
    }
}

