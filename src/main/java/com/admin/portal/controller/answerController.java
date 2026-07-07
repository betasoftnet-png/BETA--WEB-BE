package com.admin.portal.controller;

import com.admin.portal.entity.Answer;
import com.admin.portal.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/answers")
public class answerController {

    @Autowired
    private AnswerService answerService;

    @PostMapping
    public Answer submitAnswer(@RequestBody Answer answer) {
        return answerService.saveAnswer(answer);
    }

    @GetMapping
    public List<Answer> getAllAnswers() {
        return answerService.getAllAnswers();
    }

    @GetMapping("/candidate/{candidateId}")
    public List<Answer> getAnswersByCandidate(@PathVariable Long candidateId) {
        return answerService.getAnswersByCandidate(candidateId);
    }
}