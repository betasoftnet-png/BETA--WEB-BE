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
        // Upsert: update existing answer for this candidate+question, or insert new
        List<Answer> existing = answerRepository.findByCandidateId(answer.getCandidateId());
        for (Answer ex : existing) {
            if (ex.getQuestionId().equals(answer.getQuestionId())) {
                ex.setSelectedAnswer(answer.getSelectedAnswer());
                return answerRepository.save(ex);
            }
        }
        return answerRepository.save(answer);
    }

    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    public List<Answer> getAnswersByCandidate(Long candidateId) {
        return answerRepository.findByCandidateId(candidateId);
    }
}