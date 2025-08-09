package net.devstudy.resume.service.impl;

import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.service.FindProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DummyFindProfileService implements FindProfileService {
    @Override public Profile findByUid(String uid) { return null; }
    @Override public Page<Profile> findAll(Pageable pageable) { return new PageImpl<>(List.of(), pageable, 0); }
    @Override public Page<Profile> findBySearchQuery(String query, Pageable pageable) { return new PageImpl<>(List.of(), pageable, 0); }
    @Override public void restoreAccess(String anyUniqueId) { }
    @Override public Profile findByRestoreToken(String token) { return new Profile(1L, true); }
}
