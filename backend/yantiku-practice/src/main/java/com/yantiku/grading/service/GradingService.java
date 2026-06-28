package com.yantiku.module.grading.service;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.module.grading.model.dto.ComprehensiveSubmitDTO;
import com.yantiku.module.grading.model.vo.GradingTaskVO;
import com.yantiku.module.practice.model.vo.AnswerResultVO;
import com.yantiku.module.question.model.entity.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * 判分服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GradingService {

    /**
     * 选择题判分
     * 
     * @param userAnswer 用户答案（如 "A" 或 "AB"）
     * @param question 题目实体
     * @return 判分结果
     */
    public AnswerResultVO gradeChoice(String userAnswer, Question question) {
        log.info("选择题判分: questionId={}, userAnswer={}", question.getId(), userAnswer);
        
        try {
            // 解析正确答案
            String correctAnswer = parseChoiceAnswer(question.getAnswer());
            
            // 标准化答案（排序字母）
            String normalizedUserAnswer = normalizeChoiceAnswer(userAnswer);
            String normalizedCorrectAnswer = normalizeChoiceAnswer(correctAnswer);
            
            // 比较答案
            boolean isCorrect = normalizedUserAnswer.equalsIgnoreCase(normalizedCorrectAnswer);
            
            // 计算得分
            BigDecimal score = isCorrect ? BigDecimal.TEN : BigDecimal.ZERO;
            BigDecimal maxScore = BigDecimal.TEN;
            
            AnswerResultVO result = AnswerResultVO.builder()
                    .isCorrect(isCorrect)
                    .score(score)
                    .maxScore(maxScore)
                    .correctAnswer(correctAnswer)
                    .analysis(question.getAnalysis())
                    .aiFeedback(null)
                    .build();
            
            log.info("选择题判分完成: questionId={}, isCorrect={}, score={}", 
                    question.getId(), isCorrect, score);
            
            return result;
            
        } catch (Exception e) {
            log.error("选择题判分失败: questionId=" + question.getId(), e);
            throw new RuntimeException("判分失败", e);
        }
    }

    /**
     * 填空题判分
     * 
     * @param userAnswer 用户答案（JSON格式，如 {"1": "答案1", "2": "答案2"}）
     * @param question 题目实体
     * @return 判分结果
     */
    public AnswerResultVO gradeFillBlank(String userAnswer, Question question) {
        log.info("填空题判分: questionId={}", question.getId());
        
        try {
            // 解析用户答案和正确答案
            Map<Integer, String> userAnswers = parseFillBlankAnswer(userAnswer);
            Map<Integer, String> correctAnswers = parseFillBlankAnswer(question.getAnswer());
            
            // 逐空判分
            int totalBlanks = correctAnswers.size();
            int correctBlanks = 0;
            Map<Integer, Boolean> blankResults = new TreeMap<>();
            
            for (Map.Entry<Integer, String> entry : correctAnswers.entrySet()) {
                Integer blankIndex = entry.getKey();
                String correctAnswer = entry.getValue();
                String userAnswerForBlank = userAnswers.get(blankIndex);
                
                boolean isBlankCorrect = gradeSingleBlank(userAnswerForBlank, correctAnswer);
                blankResults.put(blankIndex, isBlankCorrect);
                
                if (isBlankCorrect) {
                    correctBlanks++;
                }
            }
            
            // 计算得分
            boolean isCorrect = (correctBlanks == totalBlanks);
            BigDecimal score = BigDecimal.TEN
                    .multiply(BigDecimal.valueOf(correctBlanks))
                    .divide(BigDecimal.valueOf(totalBlanks), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal maxScore = BigDecimal.TEN;
            
            AnswerResultVO result = AnswerResultVO.builder()
                    .isCorrect(isCorrect)
                    .score(score)
                    .maxScore(maxScore)
                    .correctAnswer(correctAnswers)
                    .analysis(question.getAnalysis())
                    .aiFeedback(blankResults)
                    .build();
            
            log.info("填空题判分完成: questionId={}, correctBlanks={}/{}, score={}", 
                    question.getId(), correctBlanks, totalBlanks, score);
            
            return result;
            
        } catch (Exception e) {
            log.error("填空题判分失败: questionId=" + question.getId(), e);
            throw new RuntimeException("判分失败", e);
        }
    }

    /**
     * 提交综合题判分任务（异步）
     * 
     * @param userId 用户ID
     * @param dto 提交数据
     * @return 判分任务信息
     */
    public GradingTaskVO submitComprehensive(Long userId, ComprehensiveSubmitDTO dto) {
        log.info("提交综合题判分任务: userId={}, questionId={}, sessionId={}", 
                userId, dto.getQuestionId(), dto.getSessionId());
        
        // 生成任务ID
        String taskId = "grading_" + System.currentTimeMillis() + "_" + userId;
        
        // TODO: 实际实现应该发送到消息队列（如RabbitMQ）
        // 目前先返回pending状态
        
        log.info("综合题判分任务已创建: taskId={}", taskId);
        
        return GradingTaskVO.builder()
                .taskId(taskId)
                .status("pending")
                .estimatedSeconds(3)
                .build();
    }

    /**
     * 查询综合题判分结果
     * 
     * @param taskId 任务ID
     * @return 判分结果
     */
    public ApiResponse<?> getComprehensiveResult(String taskId) {
        log.info("查询综合题判分结果: taskId={}", taskId);
        
        // TODO: 实际实现应该从Redis或数据库查询任务状态
        // 目前返回pending状态
        
        return ApiResponse.ok(GradingTaskVO.builder()
                .taskId(taskId)
                .status("pending")
                .estimatedSeconds(3)
                .build());
    }

    /**
     * 解析选择题答案
     */
    private String parseChoiceAnswer(String answerJson) {
        // 简单实现：假设answer字段直接存储答案如 "A" 或 "AB"
        // 实际应该解析JSON
        return answerJson.replaceAll("[\\[\\]\"\\s]", "");
    }

    /**
     * 标准化选择题答案（排序字母）
     */
    private String normalizeChoiceAnswer(String answer) {
        if (answer == null || answer.length() <= 1) {
            return answer;
        }
        
        // 将答案拆分为字符数组，排序后重新组合
        char[] chars = answer.toCharArray();
        java.util.Arrays.sort(chars);
        return new String(chars);
    }

    /**
     * 解析填空题答案
     */
    private Map<Integer, String> parseFillBlankAnswer(String answerJson) {
        // 简单实现：假设格式为 {"1": "答案1", "2": "答案2"}
        // 实际应该使用Jackson解析JSON
        Map<Integer, String> result = new TreeMap<>();
        
        try {
            // 简单解析（生产环境应使用Jackson）
            String[] pairs = answerJson.replaceAll("[{}\"]", "").split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    result.put(Integer.parseInt(keyValue[0].trim()), keyValue[1].trim());
                }
            }
        } catch (Exception e) {
            log.error("解析填空题答案失败: " + answerJson, e);
        }
        
        return result;
    }

    /**
     * 判分单个填空
     */
    private boolean gradeSingleBlank(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }
        
        // 精确匹配
        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            return true;
        }
        
        // 数学等价判断（简单实现）
        if (isMathExpression(userAnswer) || isMathExpression(correctAnswer)) {
            try {
                double userValue = evaluateMath(userAnswer);
                double correctValue = evaluateMath(correctAnswer);
                return Math.abs(userValue - correctValue) < 0.0001;
            } catch (Exception e) {
                // 计算失败，继续模糊匹配
            }
        }
        
        // 模糊匹配（去除空格、标点符号后比较）
        String normalizedUser = userAnswer.replaceAll("[\\s\\p{Punct}]", "");
        String normalizedCorrect = correctAnswer.replaceAll("[\\s\\p{Punct}]", "");
        
        return normalizedUser.equalsIgnoreCase(normalizedCorrect);
    }

    /**
     * 判断是否为数学表达式
     */
    private boolean isMathExpression(String str) {
        return str != null && (str.contains("+") || str.contains("-") || 
                               str.contains("*") || str.contains("/") || str.contains("^"));
    }

    /**
     * 计算简单数学表达式（非常简化的实现）
     */
    private double evaluateMath(String expr) {
        // 生产环境应使用表达式解析库如exp4j
        // 这里仅做演示
        expr = expr.replaceAll("\\s", "");
        
        try {
            // 简单支持加减乘除
            if (expr.contains("+")) {
                String[] parts = expr.split("\\+");
                return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
            }
            if (expr.contains("-")) {
                String[] parts = expr.split("-");
                return Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);
            }
            if (expr.contains("*")) {
                String[] parts = expr.split("\\*");
                return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
            }
            if (expr.contains("/")) {
                String[] parts = expr.split("/");
                return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
            }
            
            return Double.parseDouble(expr);
        } catch (Exception e) {
            throw new RuntimeException("无法计算数学表达式: " + expr, e);
        }
    }
}
