package com.yantiku.module.diagnostic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yantiku.common.exception.NotFoundException;
import com.yantiku.module.diagnostic.mapper.DiagnosticReportMapper;
import com.yantiku.module.diagnostic.model.entity.DiagnosticReport;
import com.yantiku.module.diagnostic.model.vo.DiagnosticReportVO;
import com.yantiku.module.practice.mapper.PracticeSessionMapper;
import com.yantiku.module.practice.mapper.UserAnswerMapper;
import com.yantiku.module.practice.model.entity.PracticeSession;
import com.yantiku.module.practice.model.entity.UserAnswer;
import com.yantiku.module.question.mapper.KnowledgePointMapper;
import com.yantiku.module.question.mapper.QuestionMapper;
import com.yantiku.module.question.model.entity.KnowledgePoint;
import com.yantiku.module.question.model.entity.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 诊断报告服务
 *
 * 提供诊断报告查询、生成等功能。
 * 报告生成基于练习会话的答题记录，计算各维度得分、薄弱点、优势点、建议等
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosticService {

    private final DiagnosticReportMapper diagnosticReportMapper;
    private final PracticeSessionMapper practiceSessionMapper;
    private final UserAnswerMapper userAnswerMapper;
    private final QuestionMapper questionMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 分页查询用户诊断报告
     *
     * @param userId 用户 ID
     * @param offset 偏移量（默认 0）
     * @param limit  每页数量（默认 10）
     * @return 报告列表
     */
    public List<DiagnosticReportVO> getReports(Long userId, int offset, int limit) {
        log.info("查询诊断报告列表: userId={}, offset={}, limit={}", userId, offset, limit);

        List<DiagnosticReport> reports = diagnosticReportMapper.findByUserId(userId, offset, limit);
        return reports.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户最新一份诊断报告
     */
    public DiagnosticReportVO getLatestReport(Long userId) {
        List<DiagnosticReport> list = diagnosticReportMapper.findByUserId(userId, 0, 1);
        if (list.isEmpty()) return null;
        return convertToVO(list.get(0));
    }

    /**
     * 根据 ID 查询诊断报告
     *
     * @param reportId 报告 ID
     * @return 报告 VO
     */
    public DiagnosticReportVO getReport(Long reportId) {
        log.info("查询诊断报告: reportId={}", reportId);

        DiagnosticReport report = diagnosticReportMapper.findById(reportId);
        if (report == null) {
            throw new NotFoundException("诊断报告不存在");
        }
        return convertToVO(report);
    }

    /**
     * 生成诊断报告
     * <p>
     * 基于练习会话的答题记录，计算：
     * <ul>
     *   <li>各科目正确率（雷达图数据）</li>
     *   <li>薄弱知识点（正确率最低的 Top 3）</li>
     *   <li>优势知识点（正确率最高的 Top 3）</li>
     *   <li>时间分析（各题型的平均耗时）</li>
     *   <li>改进建议</li>
     * </ul>
     *
     * @param userId    用户 ID
     * @param sessionId 练习会话 ID
     * @return 生成的报告 VO
     */
    @Transactional(rollbackFor = Exception.class)
    public DiagnosticReportVO generateReport(Long userId, Long sessionId) {
        log.info("生成诊断报告: userId={}, sessionId={}", userId, sessionId);

        PracticeSession session = practiceSessionMapper.findById(sessionId);
        if (session == null) {
            throw new NotFoundException("练习会话不存在");
        }

        List<UserAnswer> answers = userAnswerMapper.findBySessionId(sessionId);

        // 计算统计数据
        Map<String, Object> radarData;
        List<Map<String, Object>> weakPoints;
        List<Map<String, Object>> strengths;
        List<Map<String, Object>> suggestions;
        Map<String, Object> timeAnalysis;
        BigDecimal overallScore;
        String abilityRating;

        if (answers == null || answers.isEmpty()) {
            // 无答题记录时使用样本数据
            log.warn("会话无答题记录，生成样本报告: sessionId={}", sessionId);
            radarData = buildSampleRadarData();
            weakPoints = Collections.emptyList();
            strengths = Collections.emptyList();
            suggestions = buildSampleSuggestions();
            timeAnalysis = buildSampleTimeAnalysis();
            overallScore = BigDecimal.ZERO;
            abilityRating = "D";
        } else {
            // 加载题目信息以获取科目和知识点
            Map<Long, Question> questionMap = loadQuestions(answers);

            // 按科目统计正确率（雷达图）
            Map<Long, SubjectStats> subjectStatsMap = calculateSubjectStats(answers, questionMap);
            radarData = buildRadarData(subjectStatsMap);

            // 按知识点统计正确率
            Map<Long, KnowledgePointStats> kpStatsMap = calculateKnowledgePointStats(answers, questionMap);

            // 薄弱点（正确率最低 Top 3）
            weakPoints = buildWeakPoints(kpStatsMap);

            // 优势点（正确率最高 Top 3）
            strengths = buildStrengths(kpStatsMap);

            // 时间分析（按题型统计平均耗时）
            timeAnalysis = buildTimeAnalysis(answers, questionMap);

            // 改进建议
            suggestions = buildSuggestions(weakPoints);

            // 总体得分和评级
            overallScore = calculateOverallScore(answers);
            abilityRating = calculateAbilityRating(overallScore);
        }

        // 构建实体并插入
        LocalDateTime generatedAt = LocalDateTime.now();
        DiagnosticReport report = DiagnosticReport.builder()
                .userId(userId)
                .sessionId(sessionId)
                .subjectId(session.getSubjectId())
                .reportType("session")
                .overallScore(overallScore)
                .abilityRating(abilityRating)
                .radarData(toJson(radarData))
                .weakPoints(toJson(weakPoints))
                .strengths(toJson(strengths))
                .suggestions(toJson(suggestions))
                .timeAnalysis(toJson(timeAnalysis))
                .generatedAt(generatedAt)
                .build();

        diagnosticReportMapper.insert(report);
        log.info("诊断报告生成成功: reportId={}, sessionId={}", report.getId(), sessionId);

        // 返回 VO（直接使用已构建的对象，避免反序列化）
        return DiagnosticReportVO.builder()
                .id(report.getId())
                .userId(userId)
                .sessionId(sessionId)
                .subjectId(session.getSubjectId())
                .reportType("session")
                .overallScore(overallScore)
                .abilityRating(abilityRating)
                .radarData(radarData)
                .weakPoints(weakPoints)
                .strengths(strengths)
                .suggestions(suggestions)
                .timeAnalysis(timeAnalysis)
                .generatedAt(generatedAt)
                .build();
    }

    // ========================================================
    // 统计计算方法
    // ========================================================

    /**
     * 批量加载答题记录关联的题目
     */
    private Map<Long, Question> loadQuestions(List<UserAnswer> answers) {
        Map<Long, Question> questionMap = new HashMap<>();
        for (UserAnswer answer : answers) {
            if (answer.getQuestionId() != null && !questionMap.containsKey(answer.getQuestionId())) {
                try {
                    Question question = questionMapper.findById(answer.getQuestionId());
                    if (question != null) {
                        questionMap.put(answer.getQuestionId(), question);
                    }
                } catch (Exception e) {
                    log.warn("查询题目失败: questionId={}", answer.getQuestionId());
                }
            }
        }
        return questionMap;
    }

    /**
     * 按科目统计正确率
     */
    private Map<Long, SubjectStats> calculateSubjectStats(List<UserAnswer> answers,
                                                           Map<Long, Question> questionMap) {
        Map<Long, SubjectStats> statsMap = new HashMap<>();
        for (UserAnswer answer : answers) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null || question.getSubjectId() == null) continue;

            SubjectStats stats = statsMap.computeIfAbsent(question.getSubjectId(), k -> new SubjectStats());
            stats.total++;
            if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                stats.correct++;
            }
        }
        return statsMap;
    }

    /**
     * 按知识点统计正确率
     */
    private Map<Long, KnowledgePointStats> calculateKnowledgePointStats(List<UserAnswer> answers,
                                                                         Map<Long, Question> questionMap) {
        Map<Long, KnowledgePointStats> statsMap = new HashMap<>();
        for (UserAnswer answer : answers) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null || question.getKnowledgePointId() == null) continue;

            Long kpId = question.getKnowledgePointId();
            KnowledgePointStats stats = statsMap.computeIfAbsent(kpId, k -> new KnowledgePointStats());
            stats.knowledgePointId = kpId;
            stats.total++;
            if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                stats.correct++;
            }
        }

        // 填充知识点名称
        for (KnowledgePointStats stats : statsMap.values()) {
            try {
                KnowledgePoint kp = knowledgePointMapper.findById(stats.knowledgePointId);
                if (kp != null) {
                    stats.knowledgePointName = kp.getName();
                }
            } catch (Exception e) {
                log.warn("查询知识点失败: kpId={}", stats.knowledgePointId);
            }
        }

        return statsMap;
    }

    /**
     * 构建雷达图数据（各科目正确率）
     */
    private Map<String, Object> buildRadarData(Map<Long, SubjectStats> subjectStatsMap) {
        List<Map<String, Object>> axes = new ArrayList<>();
        for (Map.Entry<Long, SubjectStats> entry : subjectStatsMap.entrySet()) {
            SubjectStats stats = entry.getValue();
            double accuracy = stats.total > 0
                    ? Math.round((double) stats.correct / stats.total * 1000) / 10.0
                    : 0.0;
            Map<String, Object> axis = new LinkedHashMap<>();
            axis.put("subjectId", entry.getKey());
            axis.put("label", "科目" + entry.getKey());
            axis.put("value", accuracy);
            axes.add(axis);
        }
        Map<String, Object> radarData = new LinkedHashMap<>();
        radarData.put("axes", axes);
        return radarData;
    }

    /**
     * 构建薄弱点（正确率最低 Top 3）
     */
    private List<Map<String, Object>> buildWeakPoints(Map<Long, KnowledgePointStats> kpStatsMap) {
        return kpStatsMap.values().stream()
                .filter(s -> s.total > 0)
                .sorted(Comparator.comparingDouble(KnowledgePointStats::getAccuracy))
                .limit(3)
                .map(stats -> {
                    Map<String, Object> point = new LinkedHashMap<>();
                    point.put("knowledgePointId", stats.knowledgePointId);
                    point.put("knowledgePointName", stats.knowledgePointName != null ? stats.knowledgePointName : "未知知识点");
                    point.put("accuracy", stats.getAccuracy());
                    point.put("total", stats.total);
                    point.put("correct", stats.correct);
                    return point;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建优势点（正确率最高 Top 3）
     */
    private List<Map<String, Object>> buildStrengths(Map<Long, KnowledgePointStats> kpStatsMap) {
        return kpStatsMap.values().stream()
                .filter(s -> s.total > 0)
                .sorted(Comparator.comparingDouble(KnowledgePointStats::getAccuracy).reversed())
                .limit(3)
                .map(stats -> {
                    Map<String, Object> point = new LinkedHashMap<>();
                    point.put("knowledgePointId", stats.knowledgePointId);
                    point.put("knowledgePointName", stats.knowledgePointName != null ? stats.knowledgePointName : "未知知识点");
                    point.put("accuracy", stats.getAccuracy());
                    point.put("total", stats.total);
                    point.put("correct", stats.correct);
                    return point;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建时间分析（按题型统计平均耗时）
     */
    private Map<String, Object> buildTimeAnalysis(List<UserAnswer> answers,
                                                    Map<Long, Question> questionMap) {
        // 按题型分组统计耗时
        Map<Integer, List<Integer>> typeTimeMap = new HashMap<>();
        for (UserAnswer answer : answers) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null || answer.getTimeSpent() == null) continue;

            int questionType = question.getQuestionType() != null ? question.getQuestionType() : 0;
            typeTimeMap.computeIfAbsent(questionType, k -> new ArrayList<>())
                    .add(answer.getTimeSpent());
        }

        List<Map<String, Object>> typeAnalysis = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : typeTimeMap.entrySet()) {
            List<Integer> times = entry.getValue();
            double avgTime = times.stream().mapToInt(Integer::intValue).average().orElse(0);
            Map<String, Object> analysis = new LinkedHashMap<>();
            analysis.put("questionType", entry.getKey());
            analysis.put("questionTypeName", getQuestionTypeName(entry.getKey()));
            analysis.put("avgTimeSeconds", Math.round(avgTime * 10) / 10.0);
            analysis.put("count", times.size());
            typeAnalysis.add(analysis);
        }

        // 总耗时
        int totalTime = answers.stream()
                .filter(a -> a.getTimeSpent() != null)
                .mapToInt(UserAnswer::getTimeSpent)
                .sum();

        Map<String, Object> timeAnalysis = new LinkedHashMap<>();
        timeAnalysis.put("totalTimeSeconds", totalTime);
        timeAnalysis.put("avgTimePerQuestion", answers.isEmpty() ? 0
                : Math.round((double) totalTime / answers.size() * 10) / 10.0);
        timeAnalysis.put("byQuestionType", typeAnalysis);
        return timeAnalysis;
    }

    /**
     * 构建改进建议
     */
    private List<Map<String, Object>> buildSuggestions(List<Map<String, Object>> weakPoints) {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        for (Map<String, Object> weakPoint : weakPoints) {
            String kpName = (String) weakPoint.get("knowledgePointName");
            double accuracy = (double) weakPoint.get("accuracy");
            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("type", "weak_point");
            suggestion.put("target", kpName);
            suggestion.put("description", String.format("知识点「%s」正确率仅 %.1f%%，建议针对性复习", kpName, accuracy));
            suggestion.put("priority", accuracy < 30 ? "high" : accuracy < 60 ? "medium" : "low");
            suggestions.add(suggestion);
        }

        if (suggestions.isEmpty()) {
            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("type", "general");
            suggestion.put("target", "整体");
            suggestion.put("description", "继续保持练习，提升答题准确率");
            suggestion.put("priority", "low");
            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 计算总体得分（正确率 × 100）
     */
    private BigDecimal calculateOverallScore(List<UserAnswer> answers) {
        int total = answers.size();
        long correct = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
        double score = total > 0 ? (double) correct / total * 100 : 0;
        return BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 根据得分计算能力评级
     */
    private String calculateAbilityRating(BigDecimal score) {
        int s = score.intValue();
        if (s >= 90) return "S";
        if (s >= 80) return "A";
        if (s >= 70) return "B";
        if (s >= 60) return "C";
        return "D";
    }

    // ========================================================
    // 样本数据（无答题记录时使用）
    // ========================================================

    private Map<String, Object> buildSampleRadarData() {
        Map<String, Object> radarData = new LinkedHashMap<>();
        radarData.put("axes", Collections.emptyList());
        radarData.put("note", "无答题记录，暂无雷达图数据");
        return radarData;
    }

    private List<Map<String, Object>> buildSampleSuggestions() {
        Map<String, Object> suggestion = new LinkedHashMap<>();
        suggestion.put("type", "general");
        suggestion.put("target", "整体");
        suggestion.put("description", "本次练习暂无答题记录，完成练习后将生成详细诊断报告");
        suggestion.put("priority", "low");
        return Collections.singletonList(suggestion);
    }

    private Map<String, Object> buildSampleTimeAnalysis() {
        Map<String, Object> timeAnalysis = new LinkedHashMap<>();
        timeAnalysis.put("totalTimeSeconds", 0);
        timeAnalysis.put("avgTimePerQuestion", 0);
        timeAnalysis.put("byQuestionType", Collections.emptyList());
        return timeAnalysis;
    }

    // ========================================================
    // 工具方法
    // ========================================================

    /**
     * 将实体转换为 VO（解析 JSON 字符串为对象）
     */
    private DiagnosticReportVO convertToVO(DiagnosticReport report) {
        return DiagnosticReportVO.builder()
                .id(report.getId())
                .userId(report.getUserId())
                .sessionId(report.getSessionId())
                .subjectId(report.getSubjectId())
                .reportType(report.getReportType())
                .overallScore(report.getOverallScore())
                .abilityRating(report.getAbilityRating())
                .radarData(parseJson(report.getRadarData()))
                .weakPoints(parseJson(report.getWeakPoints()))
                .strengths(parseJson(report.getStrengths()))
                .suggestions(parseJson(report.getSuggestions()))
                .timeAnalysis(parseJson(report.getTimeAnalysis()))
                .generatedAt(report.getGeneratedAt())
                .build();
    }

    /**
     * 解析 JSON 字符串为对象
     */
    private Object parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            log.warn("JSON 解析失败: {}", e.getMessage());
            return json;
        }
    }

    /**
     * 将对象序列化为 JSON 字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON 序列化失败: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 获取题型名称
     */
    private String getQuestionTypeName(int questionType) {
        switch (questionType) {
            case 1: return "选择题";
            case 2: return "填空题";
            case 3: return "综合题";
            default: return "未知题型";
        }
    }

    // ========================================================
    // 内部统计类
    // ========================================================

    /** 科目统计 */
    private static class SubjectStats {
        int total;
        int correct;
    }

    /** 知识点统计 */
    private static class KnowledgePointStats {
        Long knowledgePointId;
        String knowledgePointName;
        int total;
        int correct;

        double getAccuracy() {
            return total > 0 ? Math.round((double) correct / total * 1000) / 10.0 : 0.0;
        }
    }
}
