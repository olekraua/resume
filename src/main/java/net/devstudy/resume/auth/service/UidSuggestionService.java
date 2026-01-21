package net.devstudy.resume.auth.service;

import java.util.List;

public interface UidSuggestionService {

    List<String> suggest(String baseUid);
}
