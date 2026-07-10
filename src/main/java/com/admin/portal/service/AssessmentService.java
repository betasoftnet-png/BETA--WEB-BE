package com.admin.portal.service;

import com.admin.portal.dto.request.QuestionDTO;
import com.admin.portal.entity.Assessment;
import com.admin.portal.entity.Question;
import com.admin.portal.entity.JobApplication;
import com.admin.portal.repository.AssessmentRepository;
import com.admin.portal.repository.QuestionRepository;
import com.admin.portal.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    // Save selected questions for a candidate
    @Transactional
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
    @Transactional
    public List<QuestionDTO> getQuestionsForCandidate(Long candidateId) {
        JobApplication application = jobApplicationRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate application not found."));

        if (Boolean.TRUE.equals(application.getAssessmentSubmitted())) {
            throw new RuntimeException("You have already submitted this assessment. Submission is allowed only once.");
        }

        if (application.getAssessmentAttempts() >= 2) {
            throw new RuntimeException("You have already started or accessed this assessment 2 times. You are not allowed to attend or submit again.");
        }

        // Increment attempts on fetching questions (initial start/access)
        application.setAssessmentAttempts(application.getAssessmentAttempts() + 1);
        jobApplicationRepository.save(application);

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

    @Transactional
    public Integer incrementAssessmentAttempts(Long candidateId) {
        JobApplication application = jobApplicationRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate application not found."));

        if (Boolean.TRUE.equals(application.getAssessmentSubmitted())) {
            throw new RuntimeException("Assessment already submitted.");
        }

        int newAttempts = application.getAssessmentAttempts() + 1;
        application.setAssessmentAttempts(newAttempts);
        jobApplicationRepository.save(application);
        return newAttempts;
    }

    @Transactional
    public void submitAssessment(Long candidateId) {
        JobApplication application = jobApplicationRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate application not found."));

        application.setAssessmentSubmitted(true);
        jobApplicationRepository.save(application);
    }
}