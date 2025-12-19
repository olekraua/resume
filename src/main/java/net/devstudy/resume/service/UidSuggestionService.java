package net.devstudy.resume.service;

import java.util.List;

public interface UidSuggestionService {

    List<String> suggest(String baseUid);
}
