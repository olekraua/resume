package net.devstudy.resume.service.impl;

import org.springframework.data.domain.Sort;

import net.devstudy.resume.component.DataBuilder;
import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.repository.search.ProfileSearchRepository;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.service.FindProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FindProfileServiceImpl implements FindProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileSearchRepository profileSearchRepository;
    protected final DataBuilder dataBuilder;

    @Value("${application.host}")
    private String appHost;

    public FindProfileServiceImpl(
            ProfileRepository profileRepository,
            ProfileSearchRepository profileSearchRepository,
            DataBuilder dataBuilder
    ) {
        this.profileRepository = profileRepository;
        this.profileSearchRepository = profileSearchRepository;
        this.dataBuilder = dataBuilder;
    }

    @Override
    public Profile findByUid(String uid) {
        return profileRepository.findByUid(uid.toLowerCase());
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAllByCompletedTrue(pageable);
    }

    @Override
public Page<Profile> findBySearchQuery(String query, Pageable pageable) {
    Pageable sorted = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by("uid").descending()
    );
    return profileSearchRepository.searchByQuery(query, sorted);
}
}
