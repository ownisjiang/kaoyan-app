package com.yantiku.module.grading.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * DeepSeek LLM 服务 — 用于填空/综合题的 AI 批改
 *
 * 配置项（application.yml）:
 *   llm.api-key   — DeepSeek API Key
 *   llm.api-url   — API endpoint（默认 https://api.deepseek.com/v1）
 *   llm.model     — 模型名（默认 deepseek-chat）
 */
@Service
@Slf4j
public class LLMService {

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.api-url:https://api.deepseek.com/v1}")
    private String apiUrl;

    @Value("${llm.model:deepseek-chat}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 批改一道填空/综合题
     *
     * @param questionContent 题目内容
     * @param correctAnswer   参考答案
     * @param userAnswer      用户答案
     * @param questionType    题目类型（2=填空, 3=综合）
     * @param maxScore        满分
     * @return GradingResult 包含得分和反馈
     */
    public GradingResult grade(String questionContent, String correctAnswer,
                                String userAnswer, int questionType, int maxScore) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("LLM API Key 未配置，使用简单匹配");
            return simpleMatch(correctAnswer, userAnswer, maxScore);
        }

        String prompt = buildPrompt(questionContent, correctAnswer, userAnswer, questionType, maxScore);

        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("temperature", 0.1);
            body.put("max_tokens", 500);
            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            msg.put("content", prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                return parseGradingResponse(content, maxScore);
            } else {
                log.error("LLM API error: {} {}", response.statusCode(), response.body());
                return simpleMatch(correctAnswer, userAnswer, maxScore);
            }
        } catch (Exception e) {
            log.error("LLM grading failed, falling back to simple match", e);
            return simpleMatch(correctAnswer, userAnswer, maxScore);
        }
    }

    private String buildPrompt(String question, String correctAnswer, String userAnswer,
                                int questionType, int maxScore) {
        String typeName = questionType == 2 ? "填空题" : "综合题";
        return String.format("""
            你是一名考研%s批改老师。请根据参考答案批改学生的答案，给出得分和评语。

            题目：%s
            参考答案：%s
            学生答案：%s
            满分：%d分

            请严格按以下JSON格式回复，不要包含任何其他内容：
            {"score": 整数(0-%d), "feedback": "评语(20字以内)"}

            批改原则：
            - 答案意思正确即可得分，不需要逐字匹配
            - 部分正确给部分分
            - 完全错误给0分
            - 反馈要简短精炼
            """, typeName, truncate(question, 500), truncate(correctAnswer, 500),
            truncate(userAnswer, 500), maxScore, maxScore);
    }

    private GradingResult parseGradingResponse(String content, int maxScore) {
        try {
            // 尝试提取 JSON
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start) {
                String json = content.substring(start, end + 1);
                JsonNode node = mapper.readTree(json);
                int score = Math.min(maxScore, Math.max(0, node.path("score").asInt(0)));
                String feedback = node.path("feedback").asText("AI批改完成");
                return new GradingResult(score, feedback);
            }
        } catch (Exception e) {
            log.warn("Failed to parse LLM response, using fallback");
        }
        return simpleMatch("", "", maxScore);
    }

    private GradingResult simpleMatch(String correct, String user, int maxScore) {
        if (correct == null || user == null) return new GradingResult(0, "答案为空");
        String c = correct.trim().toLowerCase();
        String u = user.trim().toLowerCase();
        if (c.equals(u)) return new GradingResult(maxScore, "完全正确");
        if (c.contains(u) || u.contains(c)) return new GradingResult(maxScore * 2 / 3, "部分正确");
        return new GradingResult(0, "答案不正确");
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }

    /**
     * AI 批改结果
     */
    public record GradingResult(int score, String feedback) {}
}
