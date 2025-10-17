package net.devstudy.resume.service.impl;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.repository.search.ProfileSearchRepository;
import net.devstudy.resume.service.ProfileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class ProfileServiceImpl implements ProfileService, UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);
 
    private final ProfileRepository profileRepository;
    private final ProfileSearchRepository profileSearchRepository;


    public ProfileServiceImpl(ProfileRepository profileRepository, ProfileSearchRepository profileSearchRepository) {
        this.profileRepository = profileRepository;
        this.profileSearchRepository = profileSearchRepository;
    }

    @Override
    public Profile findByUid(String uid) {
        return profileRepository.findByUid(uid).orElse(null);
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Iterable<Profile> findAllForIndexing(){
        Iterable<Profile> all = profileRepository.findAll();
        for (Profile p : all){
            p.getSkills().size();
            p.getCertificates().size();
            p.getLanguages().size();
            p.getPractics().size();
            p.getCourses().size();

        }
        return all;
    }

    @Override
    public Page<Profile> findBySearchQuery(String query, Pageable pageable) {
        return profileSearchRepository.findByObjectiveLikeOrSummaryLikeOrPracticsCompanyLikeOrPracticsPositionLike(
                query, query, query, query, pageable);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Profile profile = findProfile(username);
        if (profile != null) {
            return new CurrentProfile(profile);
        }
        LOGGER.error("Profile not found by {}", username);
        throw new UsernameNotFoundException("Profile not found by " + username);
    }

    private Profile findProfile(String anyUniqueId) {
        Profile profile = profileRepository.findByUid(anyUniqueId);
        if (profile == null) {
            profile = profileRepository.findByEmail(anyUniqueId);
            if (profile == null) {
                profile = profileRepository.findByPhone(anyUniqueId);
            }
        }
        return profile;
    }




}
