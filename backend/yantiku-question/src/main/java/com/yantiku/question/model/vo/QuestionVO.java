package com.yantiku.module.question.model.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yantiku.module.question.model.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 题目视图对象
 * 
 * 用于 API 响应，不包含答案（安全考虑）
 * 答案仅在需要评分的详细接口中返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVO {
    
    /**
     * 题目ID
     */
    private Long id;
    
    /**
     * 科目ID
     */
    private Long subjectId;
    
    /**
     * 知识点ID
     */
    private Long knowledgePointId;
    
    /**
     * 题目类型（1=选择题 2=填空题 3=综合题）
     */
    private Integer questionType;
    
    /**
     * 难度等级（1-5）
     */
    private Integer difficulty;
    
    /**
     * 考题年份
     */
    private Integer examYear;
    
    /**
     * 题目来源
     */
    private String source;
    
    /**
     * 题目内容（纯文本题干）
     */
    private Object content;
    
    /**
     * 选项列表（仅选择题有，已从 JSON 字符串解析为 List<String>）
     * 格式: ["A. xxx", "B. xxx", ...]
     */
    private List<String> options;
    
    /**
     * 答案（默认 null，仅在详细接口中返回）
     * 
     * @see QuestionDetailVO 包含答案的视图对象
     */
    private Object answer;
    
    /**
     * 解析
     */
    private String analysis;
    
    /**
     * 标签（JSON 字符串解析后的对象）
     */
    private Object tags;
    
    /**
     * 使用次数
     */
    private Integer useCount;
    
    /**
     * 正确次数
     */
    private Integer correctCount;
    
    /**
     * 正确率
     */
    private BigDecimal correctRate;
    
    /**
     * 从实体类转换（不包含答案）
     * 
     * @param question 题目实体
     * @return QuestionVO（answer 字段为 null）
     */
    public static QuestionVO fromEntity(Question question) {
        if (question == null) {
            return null;
        }
        
        return QuestionVO.builder()
                .id(question.getId())
                .subjectId(question.getSubjectId())
                .knowledgePointId(question.getKnowledgePointId())
                .questionType(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .examYear(question.getExamYear())
                .source(question.getSource())
                .content(question.getContent())
                .options(parseOptions(question.getOptions()))
                .answer(null) // 不返回答案
                .analysis(question.getAnalysis())
                .tags(parseJson(question.getTags()))
                .useCount(question.getUseCount())
                .correctCount(question.getCorrectCount())
                .correctRate(question.getCorrectRate())
                .build();
    }
    
    /**
     * 从实体类转换（不包含答案，别名方法）
     * 
     * @param question 题目实体
     * @return QuestionVO（answer 字段为 null）
     */
    public static QuestionVO fromEntityWithoutAnswer(Question question) {
        return fromEntity(question);
    }
    
    /**
     * 简单的 JSON 解析（根据实际需求，可以替换为 Jackson ObjectMapper）
     */
    private static Object parseJson(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        return jsonString;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析选项 JSON 字符串为 List<String>
     * 输入: "[\"A. xxx\", \"B. xxx\"]"
     * 输出: ["A. xxx", "B. xxx"]
     */
    @SuppressWarnings("unchecked")
    private static List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(optionsJson, List.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 公开静态方法，供 PracticeService 等外部调用
     */
    public static List<String> parseOptionsStatic(String optionsJson) {
        return parseOptions(optionsJson);
    }
}
