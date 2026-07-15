package com.admin.portal.service;

import com.admin.portal.entity.LikedJob;
import com.admin.portal.repository.LikedJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LikedJobService {

    @Autowired
    private LikedJobRepository likedJobRepository;

    public List<LikedJob> getLikedJobs(String email) {
        return likedJobRepository.findByEmail(email);
    }

    public LikedJob likeJob(String email, Long jobId) {
        if (likedJobRepository.existsByEmailAndJobId(email, jobId)) {
            return likedJobRepository.findByEmailAndJobId(email, jobId).orElse(null);
        }
        LikedJob likedJob = new LikedJob();
        likedJob.setEmail(email);
        likedJob.setJobId(jobId);
        return likedJobRepository.save(likedJob);
    }

    @Transactional
    public void unlikeJob(String email, Long jobId) {
        likedJobRepository.deleteByEmailAndJobId(email, jobId);
    }
}
