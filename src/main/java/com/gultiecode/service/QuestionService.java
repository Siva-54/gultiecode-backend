package com.gultiecode.service;

import com.gultiecode.dto.AssessmentRecords.QuestionDto;
import com.gultiecode.dto.AssessmentRecords.QuestionRequest;
import com.gultiecode.entity.Level;
import com.gultiecode.entity.Question;
import com.gultiecode.repository.LevelRepository;
import com.gultiecode.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final LevelRepository levelRepository;

    public QuestionService(QuestionRepository questionRepository, LevelRepository levelRepository) {
        this.questionRepository = questionRepository;
        this.levelRepository = levelRepository;
    }

    public List<QuestionDto> getQuestionsByLevel(Long levelId) {
        return questionRepository.findByLevelIdOrderByOrderIndexAsc(levelId).stream()
                .map(this::mapToQuestionDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionDto createQuestion(Long levelId, QuestionRequest request, String adminUsername) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        Question question = new Question();
        question.setLevel(level);
        question.setQuestionText(request.questionText());
        question.setOptionA(request.optionA());
        question.setOptionB(request.optionB());
        question.setOptionC(request.optionC());
        question.setOptionD(request.optionD());
        question.setCorrectAnswer(request.correctAnswer());
        question.setPoints(request.points() > 0 ? request.points() : 100);
        question.setStatus("ACTIVE");
        question.setCreatedBy(adminUsername);

        Question saved = questionRepository.save(question);
        return mapToQuestionDto(saved);
    }

    @Transactional
    public QuestionDto updateQuestion(Long questionId, QuestionRequest request, String adminUsername) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        question.setQuestionText(request.questionText());
        question.setOptionA(request.optionA());
        question.setOptionB(request.optionB());
        question.setOptionC(request.optionC());
        question.setOptionD(request.optionD());
        question.setCorrectAnswer(request.correctAnswer());
        if (request.points() > 0) question.setPoints(request.points());
        question.setUpdatedBy(adminUsername);

        Question updated = questionRepository.save(question);
        return mapToQuestionDto(updated);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new RuntimeException("Question not found with id: " + questionId);
        }
        questionRepository.deleteById(questionId);
    }

    public QuestionDto mapToQuestionDto(Question q) {
        return new QuestionDto(
                q.getId(),
                q.getLevel().getId(),
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getCorrectAnswer(),
                q.getPoints()
        );
    }
}
