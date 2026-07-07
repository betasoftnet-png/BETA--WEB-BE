package com.admin.portal.service;

import com.admin.portal.entity.Assessment;
import com.admin.portal.entity.Question;
import com.admin.portal.repository.AssessmentRepository;
import com.admin.portal.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    // Save selected questions for a candidate
    public void assignQuestions(Long candidateId, List<Long> questionIds) {

        for (Long questionId : questionIds) {

            Assessment assessment = new Assessment();
            assessment.setCandidateId(candidateId);
            assessment.setQuestionId(questionId);

            assessmentRepository.save(assessment);
        }
    }

    // Get assigned questions for a candidate
    public List<Question> getQuestionsForCandidate(Long candidateId) {

        List<Assessment> assessments = assessmentRepository.findByCandidateId(candidateId);

        List<Question> questions = new ArrayList<>();

        for (Assessment assessment : assessments) {

            Question question = questionRepository
                    .findById(assessment.getQuestionId())
                    .orElse(null);

            if (question != null) {
                questions.add(question);
            }
        }

        return questions;
    }
}