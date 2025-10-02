package net.devstudy.resume.repository.storage;

import java.util.Date;
import java.util.List;

import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import net.devstudy.resume.domain.RememberMeToken;

public class MongoPersistentTokenRepositoryAdapter implements PersistentTokenRepository {

    private final RememberMeTokenRepository rememberMeTokenRepository;

    public MongoPersistentTokenRepositoryAdapter(RememberMeTokenRepository rememberMeTokenRepository) {
        this.rememberMeTokenRepository = rememberMeTokenRepository;
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        rememberMeTokenRepository.save(new RememberMeToken(token));
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        RememberMeToken token = rememberMeTokenRepository.findBySeries(series);
        if (token == null) {
            // опційно: можна нічого не робити або кинути виняток
            return;
        }
        rememberMeTokenRepository.save(
            new RememberMeToken(token.getId(), token.getUsername(), series, tokenValue, lastUsed)
        );
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String series) {
        // Припускаємо, що RememberMeToken implements PersistentRememberMeToken
        return rememberMeTokenRepository.findBySeries(series);
    }

    @Override
    public void removeUserTokens(String username) {
        List<RememberMeToken> list = rememberMeTokenRepository.findByUsername(username);
        if (!list.isEmpty()) {
            rememberMeTokenRepository.deleteAll(list); // <-- ключова зміна для Spring Data 3
        }
    }
}

