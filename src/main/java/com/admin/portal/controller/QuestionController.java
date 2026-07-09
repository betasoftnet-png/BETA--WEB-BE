package com.admin.portal.controller;

import com.admin.portal.dto.request.QuestionDTO;
import com.admin.portal.entity.Question;
import com.admin.portal.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // Get all questions
    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    // Create question
    @PostMapping
    public Question createQuestion(@RequestBody Question question) {
        return questionService.createQuestion(question);
    }

    @PostMapping("/bulk")
    public List<Question> createQuestions(@RequestBody List<Question> questions) {
        return questionService.createQuestions(questions);
    }

    // Update question
    @PutMapping("/{id}")
    public Question updateQuestion(@PathVariable Long id,
            @RequestBody Question question) {
        return questionService.updateQuestion(id, question);
    }

    // Delete question
    @DeleteMapping("/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return "Question deleted successfully";
    }

    @GetMapping("/candidate")
    public List<QuestionDTO> getQuestionsForCandidate() {
        return questionService.getQuestionsForCandidate();
    }
}