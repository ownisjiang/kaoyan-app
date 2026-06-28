package com.yantiku.module.practice.service;

import com.yantiku.common.exception.BadRequestException;
import com.yantiku.common.exception.NotFoundException;
import com.yantiku.module.achievement.service.AchievementService;
import com.yantiku.module.diagnostic.service.DiagnosticService;
import com.yantiku.module.grading.llm.LLMService;
import com.yantiku.module.practice.mapper.PracticeSessionMapper;
import com.yantiku.module.practice.mapper.UserAnswerMapper;
import com.yantiku.module.practice.model.dto.CreateSessionDTO;
import com.yantiku.module.practice.model.dto.SubmitAnswerDTO;
import com.yantiku.module.practice.model.entity.PracticeSession;
import com.yantiku.module.practice.model.entity.UserAnswer;
import com.yantiku.module.practice.model.vo.AnswerResultVO;
import com.yantiku.module.practice.model.vo.SessionVO;
import com.yantiku.module.question.mapper.QuestionMapper;
import com.yantiku.module.question.model.entity.Question;
import com.yantiku.module.question.model.vo.QuestionVO;
import com.yantiku.module.user.mapper.DailyStatsMapper;
import com.yantiku.module.user.mapper.UserMapper;
import com.yantiku.module.user.model.entity.DailyStats;
import com.yantiku.module.user.model.entity.User;
import com.yantiku.module.wrongbook.mapper.WrongQuestionBookMapper;
import com.yantiku.module.wrongbook.model.entity.WrongQuestionBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PracticeService {

    private final PracticeSessionMapper practiceSessionMapper;
    private final UserAnswerMapper userAnswerMapper;
    private final QuestionMapper questionMapper;
    private final WrongQuestionBookMapper wrongQuestionBookMapper;
    private final DailyStatsMapper dailyStatsMapper;
    private final DiagnosticService diagnosticService;
    private final AchievementService achievementService;
    private final LLMService llmService;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public SessionVO createSession(Long userId, CreateSessionDTO dto) {
        PracticeSession session = PracticeSession.builder()
                .userId(userId)
                .subjectId(dto.getSubjectId()).mode(dto.getMode())
                .totalQuestions(dto.getTotalQuestions())
                .answeredCount(0).correctCount(0).totalScore(BigDecimal.ZERO)
                .startTime(LocalDateTime.now()).status("in_progress")
                .config(dto.getConfig()).createdAt(LocalDateTime.now()).build();
        practiceSessionMapper.insert(session);
        SessionVO vo = convertToSessionVO(session);
        vo.setQuestions(fetchPagedQuestions(dto, 0)); // 第1批=offset 0
        return vo;
    }

    public SessionVO getSession(Long sessionId) {
        PracticeSession session = practiceSessionMapper.findById(sessionId);
        if (session == null) throw new NotFoundException("session not found");
        return convertToSessionVO(session);
    }

    public List<SessionVO> getUserSessions(Long userId, int offset, int limit) {
        List<PracticeSession> sessions = practiceSessionMapper.findByUserId(userId, offset, limit);
        List<SessionVO> result = new ArrayList<>();
        for (PracticeSession s : sessions) result.add(convertToSessionVO(s));
        return result;
    }

    @Transactional
    public AnswerResultVO submitAnswer(Long userId, Long sessionId, SubmitAnswerDTO dto) {
        PracticeSession session = practiceSessionMapper.findById(sessionId);
        if (session == null) throw new NotFoundException("session not found");
        Question question = questionMapper.findById(dto.getQuestionId());
        if (question == null) throw new NotFoundException("question not found");

        boolean isCorrect = false;
        BigDecimal score = BigDecimal.ZERO;
        String correctAnswer = question.getAnswer();

        if (dto.getUserAnswer() != null && dto.getUserAnswer().equals(question.getAnswer())) {
            isCorrect = true;
            score = BigDecimal.ONE;
        }

        UserAnswer userAnswer = UserAnswer.builder()
                .sessionId(sessionId).userId(userId).questionId(dto.getQuestionId())
                .userAnswer(dto.getUserAnswer()).isCorrect(isCorrect)
                .score(score).timeSpent(dto.getTimeSpent())
                .submittedAt(LocalDateTime.now()).build();
        userAnswerMapper.insert(userAnswer);

        AnswerResultVO answerResult = AnswerResultVO.builder()
                .answerId(userAnswer.getId()).isCorrect(isCorrect)
                .score(score).correctAnswer(correctAnswer).analysis(question.getAnalysis()).build();

        if (!isCorrect) {
            WrongQuestionBook wqb = WrongQuestionBook.builder()
                    .userId(userId).questionId(dto.getQuestionId())
                    .subjectId(question.getSubjectId())
                    .wrongCount(1).consecutiveCorrect(0).lastWrongAt(LocalDateTime.now())
                    .masteryLevel(0).status("active")
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
            wrongQuestionBookMapper.upsert(wqb);
        }

        updateDailyStats(userId, isCorrect, dto.getTimeSpent());
        practiceSessionMapper.updateStatus(sessionId, session.getStatus(), session.getAnsweredCount() + 1);
        if (isCorrect) {
            practiceSessionMapper.updateScore(sessionId, session.getCorrectCount() + 1, session.getTotalScore().add(score));
        }
        // P1: 触发成就检查（异步，非阻塞）
        try {
            achievementService.checkAndUnlockAchievements(userId);
        } catch (Exception e) {
            log.warn("成就检查失败(非阻塞): userId={}, error={}", userId, e.getMessage());
        }
        return answerResult;
    }

    public void pauseSession(Long sessionId) {
        PracticeSession session = practiceSessionMapper.findById(sessionId);
        if (session == null) throw new NotFoundException("session not found");
        if (!"in_progress".equals(session.getStatus())) throw new BadRequestException("session not in progress");
        practiceSessionMapper.updateStatus(sessionId, "paused", session.getAnsweredCount());
    }

    public void resumeSession(Long sessionId) {
        PracticeSession session = practiceSessionMapper.findById(sessionId);
        if (session == null) throw new NotFoundException("session not found");
        if (!"paused".equals(session.getStatus())) throw new BadRequestException("session not paused");
        practiceSessionMapper.updateStatus(sessionId, "in_progress", session.getAnsweredCount());
    }

    @Transactional
    public SessionVO completeSession(Long sessionId) {
        PracticeSession session = practiceSessionMapper.findById(sessionId);
        if (session == null) throw new NotFoundException("session not found");
        if ("completed".equals(session.getStatus()) || "abandoned".equals(session.getStatus()))
            throw new BadRequestException("session already ended");
        LocalDateTime endTime = LocalDateTime.now();
        int durationSeconds = (int) Duration.between(session.getStartTime(), endTime).getSeconds();
        practiceSessionMapper.complete(sessionId, endTime, durationSeconds,
                session.getCorrectCount(), session.getTotalScore());

        // 用户统计已由每道题提交时的 updateStats() 原子自增，此处不再重复计算
        // 仅更新连续天数和 last_active_date
        computeAndUpdateStreak(session.getUserId(), LocalDate.now());

        // 自动生成诊断报告
        try {
            diagnosticService.generateReport(session.getUserId(), sessionId);
        } catch (Exception e) {
            log.warn("诊断报告生成失败(非阻塞): sessionId={}, error={}", sessionId, e.getMessage());
        }

        // 触发成就检查
        try {
            achievementService.checkAndUnlockAchievements(session.getUserId());
        } catch (Exception e) {
            log.warn("成就检查失败: userId={}, error={}", session.getUserId(), e.getMessage());
        }

        return convertToSessionVO(practiceSessionMapper.findById(sessionId));
    }

    public void abandonSession(Long sessionId) {
        PracticeSession session = practiceSessionMapper.findById(sessionId);
        if (session == null) throw new NotFoundException("session not found");
        if ("completed".equals(session.getStatus())) throw new BadRequestException("cannot abandon completed session");
        practiceSessionMapper.updateStatus(sessionId, "abandoned", session.getAnsweredCount());
    }

    public List<AnswerResultVO> submitAnswersBatch(Long userId, Long sessionId, List<SubmitAnswerDTO> dtos) {
        PracticeSession session = practiceSessionMapper.findById(sessionId);
        if (session == null) throw new NotFoundException("session not found");

        List<AnswerResultVO> results = new ArrayList<>();
        int batchCorrect = 0;
        BigDecimal batchScore = BigDecimal.ZERO;

        for (SubmitAnswerDTO dto : dtos) {
            Question question = questionMapper.findById(dto.getQuestionId());
            if (question == null) {
                results.add(AnswerResultVO.builder().answerId(null).isCorrect(false)
                        .score(BigDecimal.ZERO).correctAnswer("").analysis("题目不存在").build());
                continue;
            }

            boolean isCorrect;
            BigDecimal score;
            String aiFeedback = null;
            int questionType = question.getQuestionType() != null ? question.getQuestionType() : 1;

            if (questionType == 1) {
                // 选择题：直接比对答案
                isCorrect = question.getAnswer() != null && question.getAnswer().equals(dto.getUserAnswer());
                score = isCorrect ? BigDecimal.ONE : BigDecimal.ZERO;
            } else {
                // 填空/综合题：AI 批改
                int maxScore = questionType == 3 ? 10 : 1;
                LLMService.GradingResult gr = llmService.grade(
                        question.getContent(),
                        question.getAnswer(),
                        dto.getUserAnswer(),
                        questionType,
                        maxScore);
                score = BigDecimal.valueOf(gr.score());
                isCorrect = gr.score() >= maxScore * 0.6; // 60%以上算正确
                aiFeedback = gr.feedback();
            }

            if (isCorrect) batchCorrect++;
            batchScore = batchScore.add(score);

            UserAnswer ua = UserAnswer.builder()
                    .sessionId(sessionId).userId(userId).questionId(dto.getQuestionId())
                    .userAnswer(dto.getUserAnswer()).isCorrect(isCorrect)
                    .score(score).timeSpent(dto.getTimeSpent())
                    .aiFeedback(aiFeedback)
                    .submittedAt(LocalDateTime.now()).build();
            userAnswerMapper.insert(ua);

            results.add(AnswerResultVO.builder()
                    .answerId(ua.getId()).isCorrect(isCorrect).score(score)
                    .correctAnswer(question.getAnswer()).analysis(question.getAnalysis())
                    .aiFeedback(aiFeedback).build());

            // 错题处理
            if (!isCorrect) {
                WrongQuestionBook wqb = WrongQuestionBook.builder()
                        .userId(userId).questionId(dto.getQuestionId())
                        .subjectId(question.getSubjectId())
                        .wrongCount(1).consecutiveCorrect(0).lastWrongAt(LocalDateTime.now())
                        .masteryLevel(0).status("active")
                        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
                wrongQuestionBookMapper.upsert(wqb);
            }

            updateDailyStats(userId, isCorrect, dto.getTimeSpent());
        }

        // 更新会话统计
        int newAnswered = session.getAnsweredCount() + dtos.size();
        int newCorrect = session.getCorrectCount() + batchCorrect;
        BigDecimal newScore = (session.getTotalScore() != null ? session.getTotalScore() : BigDecimal.ZERO).add(batchScore);
        practiceSessionMapper.updateStatus(sessionId, session.getStatus(), newAnswered);
        practiceSessionMapper.updateScore(sessionId, newCorrect, newScore);

        // 触发成就检查
        try {
            achievementService.checkAndUnlockAchievements(userId);
        } catch (Exception e) {
            log.warn("成就检查失败: userId={}, error={}", userId, e.getMessage());
        }

        return results;
    }

    /**
     * 获取下一批题目（offset 方式）
     */
    public List<QuestionVO> getNextBatch(Long sessionId, int offset) {
        PracticeSession session = practiceSessionMapper.findById(sessionId);
        if (session == null) throw new NotFoundException("session not found");
        return fetchPagedQuestions(session.getSubjectId(), session.getMode(), offset);
    }

    private List<QuestionVO> fetchPagedQuestions(CreateSessionDTO dto, int offset) {
        return fetchPagedQuestions(dto.getSubjectId(), dto.getMode(), offset);
    }

    private List<QuestionVO> fetchPagedQuestions(Long subjectId, String mode, int offset) {
        // 模考模式：混合三种题型，20题/批（10选择 + 7填空 + 3综合）
        if ("mock_exam".equals(mode)) {
            int batch = offset / 20; // 第几批
            int choiceOffset = batch * 10;
            int fillBlankOffset = batch * 7;
            int comprehensiveOffset = batch * 3;
            List<Question> all = new java.util.ArrayList<>();
            all.addAll(questionMapper.findPagedBySubject(subjectId, 1, choiceOffset, 10));
            all.addAll(questionMapper.findPagedBySubject(subjectId, 2, fillBlankOffset, 7));
            all.addAll(questionMapper.findPagedBySubject(subjectId, 3, comprehensiveOffset, 3));
            return all.stream().map(this::toQuestionVO).toList();
        }

        int limit = 10;
        Integer questionType = switch (mode) {
            case "quick_quiz" -> 1;
            case "fill_blank" -> 2;
            case "comprehensive" -> 3;
            default -> null;
        };

        List<Question> questions = questionMapper.findPagedBySubject(subjectId, questionType, offset, limit);
        return questions.stream().map(this::toQuestionVO).toList();
    }

    private QuestionVO toQuestionVO(Question q) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setSubjectId(q.getSubjectId()); vo.setKnowledgePointId(q.getKnowledgePointId());
        vo.setQuestionType(q.getQuestionType()); vo.setDifficulty(q.getDifficulty());
        vo.setExamYear(q.getExamYear()); vo.setSource(q.getSource());
        vo.setContent(q.getContent());
        vo.setOptions(QuestionVO.parseOptionsStatic(q.getOptions()));
        vo.setAnalysis(q.getAnalysis());
        vo.setUseCount(q.getUseCount() != null ? q.getUseCount() : 0);
        vo.setCorrectCount(q.getCorrectCount() != null ? q.getCorrectCount() : 0);
        return vo;
    }

    private SessionVO convertToSessionVO(PracticeSession session) {
        return SessionVO.builder()
                .id(session.getId()).userId(session.getUserId())
                .subjectId(session.getSubjectId())
                .mode(session.getMode()).totalQuestions(session.getTotalQuestions())
                .answeredCount(session.getAnsweredCount()).correctCount(session.getCorrectCount())
                .totalScore(session.getTotalScore()).startTime(session.getStartTime())
                .endTime(session.getEndTime()).durationSeconds(session.getDurationSeconds())
                .status(session.getStatus()).build();
    }

    private void updateDailyStats(Long userId, boolean isCorrect, Integer timeSpent) {
        LocalDate today = LocalDate.now();
        DailyStats stats = dailyStatsMapper.findByUserIdAndDate(userId, today);
        if (stats == null) {
            stats = DailyStats.builder().userId(userId).statDate(today)
                    .questionsAnswered(1).correctCount(isCorrect ? 1 : 0)
                    .studyDurationSeconds(timeSpent != null ? timeSpent : 0)
                    .sessionsCount(0).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
            dailyStatsMapper.insert(stats);
        } else {
            dailyStatsMapper.incrementQuestionsAnswered(stats.getId(), 1);
            if (isCorrect) dailyStatsMapper.incrementCorrectCount(stats.getId(), 1);
            if (timeSpent != null) dailyStatsMapper.incrementStudyDuration(stats.getId(), timeSpent);
        }

        // 同步更新 users 表统计（每道题都更新，实时可见）
        try {
            userMapper.updateStats(userId, isCorrect, timeSpent != null ? timeSpent.longValue() : 0L);
        } catch (Exception e) {
            log.warn("更新用户统计失败(非阻塞): userId={}, error={}", userId, e.getMessage());
        }

        // 计算并更新连续学习天数
        computeAndUpdateStreak(userId, today);
    }

    /**
     * 计算连续学习天数：从 daily_stats 表中取活跃日期，从今天往回数连续天数
     */
    /**
     * 获取用户在各科目下的掌握度
     * @return [{subjectId, total, correct, mastery}]
     */
    public List<Map<String, Object>> getSubjectMastery(Long userId) {
        // 各科目正确数
        List<Map<String, Object>> correctList = userAnswerMapper.countCorrectBySubject(userId);
        Map<Long, Integer> correctMap = new java.util.LinkedHashMap<>();
        for (Map<String, Object> row : correctList) {
            Long subjectId = ((Number) row.get("subject_id")).longValue();
            int cnt = ((Number) row.get("correct_cnt")).intValue();
            correctMap.put(subjectId, cnt);
        }

        // 各科目总答题数
        List<Map<String, Object>> totalList = userAnswerMapper.countTotalBySubject(userId);
        Map<Long, Integer> totalMap = new java.util.LinkedHashMap<>();
        for (Map<String, Object> row : totalList) {
            Long subjectId = ((Number) row.get("subject_id")).longValue();
            int cnt = ((Number) row.get("total_cnt")).intValue();
            totalMap.put(subjectId, cnt);
        }

        // 合并计算掌握度
        java.util.Set<Long> allSubjectIds = new java.util.LinkedHashSet<>();
        allSubjectIds.addAll(correctMap.keySet());
        allSubjectIds.addAll(totalMap.keySet());

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Long sid : allSubjectIds) {
            int total = totalMap.getOrDefault(sid, 0);
            int correct = correctMap.getOrDefault(sid, 0);
            int mastery = total > 0 ? (int) Math.round((double) correct / total * 100) : 0;
            result.add(Map.of("subjectId", sid, "total", total, "correct", correct, "mastery", mastery));
        }
        return result;
    }

    private void computeAndUpdateStreak(Long userId, LocalDate today) {
        try {
            List<java.time.LocalDate> activeDates = dailyStatsMapper.countConsecutiveDays(userId, today);
            int streak = 0;
            LocalDate expected = today;
            for (java.time.LocalDate d : activeDates) {
                if (d.equals(expected)) {
                    streak++;
                    expected = expected.minusDays(1);
                } else if (d.isBefore(expected)) {
                    break; // 断开了
                }
            }
            if (streak > 0) {
                userMapper.updateStreak(userId, streak);
            }
        } catch (Exception e) {
            log.warn("计算连续天数失败(非阻塞): userId={}, error={}", userId, e.getMessage());
        }
    }
}
