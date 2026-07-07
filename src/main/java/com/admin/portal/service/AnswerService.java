package com.admin.portal.service;

import com.admin.portal.entity.Answer;
import com.admin.portal.repository.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    public Answer saveAnswer(Answer answer) {
        return answerRepository.save(answer);
    }

    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    public List<Answer> getAnswersByCandidate(Long candidateId) {
        return answerRepository.findByCandidateId(candidateId);
    }
}