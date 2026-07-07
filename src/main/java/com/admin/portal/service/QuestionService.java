package com.admin.portal.service;

import com.admin.portal.entity.Question;
import com.admin.portal.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    public Question updateQuestion(Long id, Question question) {
        Question existing = questionRepository.findById(id).orElseThrow();

        existing.setQuestion(question.getQuestion());
        existing.setOptionA(question.getOptionA());
        existing.setOptionB(question.getOptionB());
        existing.setOptionC(question.getOptionC());
        existing.setOptionD(question.getOptionD());
        existing.setCorrectAnswer(question.getCorrectAnswer());

        return questionRepository.save(existing);
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}