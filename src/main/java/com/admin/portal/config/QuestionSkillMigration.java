package com.admin.portal.config;

import com.admin.portal.entity.Question;
import com.admin.portal.repository.QuestionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * One-time idempotent startup migration for question skills.
 *
 * Backfills the `skill` column in the `questions` table for any row
 * that currently has the column blank/null, mapping by question ID range.
 */
@Component
public class QuestionSkillMigration {

    @Autowired
    private QuestionRepository questionRepository;

    @PostConstruct
    @Transactional
    public void backfillQuestionSkills() {
        List<Question> questions = questionRepository.findAll();
        if (questions.isEmpty()) {
            return;
        }

        List<Question> toUpdate = new ArrayList<>();

        for (Question question : questions) {
            Long id = question.getId();
            if (id == null) {
                continue;
            }

            // Only update if skill is currently null or empty/blank
            if (question.getSkill() == null || question.getSkill().trim().isEmpty()) {
                String skill = getSkillForId(id);
                if (skill != null) {
                    question.setSkill(skill);
                    toUpdate.add(question);
                }
            }
        }

        if (!toUpdate.isEmpty()) {
            questionRepository.saveAll(toUpdate);
            System.out.println("[QuestionSkillMigration] Backfilled skills for " + toUpdate.size() + " question(s).");
        } else {
            System.out.println("[QuestionSkillMigration] No blank question skills found. Migration skipped.");
        }
    }

    private String getSkillForId(Long id) {
        long qId = id;
        if (qId >= 896 && qId <= 925) return "React.js";
        if (qId >= 926 && qId <= 955) return "Javascript";
        if (qId >= 956 && qId <= 985) return "Node.js";
        if (qId >= 986 && qId <= 1015) return "Express.js";
        if (qId >= 1016 && qId <= 1045) return "MongoDB";
        if (qId >= 1046 && qId <= 1075) return "SQL";
        if (qId >= 1076 && qId <= 1105) return "Java Springboot";
        if (qId >= 1106 && qId <= 1135) return "Python Django";
        if (qId >= 1136 && qId <= 1165) return "DevOps";
        if (qId >= 1166 && qId <= 1195) return "C/C++";
        if (qId >= 1196 && qId <= 1225) return "HTML/CSS";
        if (qId >= 1226 && qId <= 1255) return "Digital Marketing";
        return null;
    }
}
