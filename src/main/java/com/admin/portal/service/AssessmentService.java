package com.admin.portal.service;

import com.admin.portal.dto.request.QuestionDTO;
import com.admin.portal.entity.Assessment;
import com.admin.portal.entity.Question;
import com.admin.portal.repository.AssessmentRepository;
import com.admin.portal.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    // Save selected questions for a candidate
    public void assignQuestions(Long candidateId, List<Long> questionIds, Integer duration) {

        // Delete previous assessment for this candidate
        assessmentRepository.deleteByCandidateId(candidateId);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(duration);

        for (Long questionId : questionIds) {

            Assessment assessment = new Assessment();
            assessment.setCandidateId(candidateId);
            assessment.setQuestionId(questionId);
            assessment.setDuration(duration);
            assessment.setStartTime(startTime);
            assessment.setEndTime(endTime);

            assessmentRepository.save(assessment);
        }
    }

    // Get assigned questions for a candidate (without correct answers)
    public List<QuestionDTO> getQuestionsForCandidate(Long candidateId) {

        List<Assessment> assessments = assessmentRepository.findByCandidateId(candidateId);

        List<QuestionDTO> questionDTOs = new ArrayList<>();

        for (Assessment assessment : assessments) {

            Question question = questionRepository
                    .findById(assessment.getQuestionId())
                    .orElse(null);

            if (question != null) {

                QuestionDTO dto = new QuestionDTO();

                dto.setId(question.getId());
                dto.setQuestion(question.getQuestion());
                dto.setOptionA(question.getOptionA());
                dto.setOptionB(question.getOptionB());
                dto.setOptionC(question.getOptionC());
                dto.setOptionD(question.getOptionD());
                dto.setDuration(assessment.getDuration());

                questionDTOs.add(dto);
            }
        }

        return questionDTOs;
    }
}