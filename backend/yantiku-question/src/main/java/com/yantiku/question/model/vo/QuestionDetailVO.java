package com.yantiku.module.question.model.vo;

import com.yantiku.module.question.model.entity.Question;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 题目详细视图对象
 * 
 * 继承自 QuestionVO，包含答案字段（用于评分场景）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class QuestionDetailVO extends QuestionVO {
    
    /**
     * 从实体类转换（包含答案）
     * 
     * @param question 题目实体
     * @return QuestionDetailVO（包含 answer 字段）
     */
    public static QuestionDetailVO fromEntity(Question question) {
        if (question == null) {
            return null;
        }
        
        QuestionDetailVO vo = new QuestionDetailVO();
        vo.setId(question.getId());
        vo.setSubjectId(question.getSubjectId());
        vo.setKnowledgePointId(question.getKnowledgePointId());
        vo.setQuestionType(question.getQuestionType());
        vo.setDifficulty(question.getDifficulty());
        vo.setExamYear(question.getExamYear());
        vo.setSource(question.getSource());
        vo.setContent(question.getContent());
        vo.setOptions(parseOptionsStatic(question.getOptions()));
        vo.setAnswer(question.getAnswer()); // 包含答案
        vo.setAnalysis(question.getAnalysis());
        vo.setTags(parseJson(question.getTags()));
        vo.setUseCount(question.getUseCount());
        vo.setCorrectCount(question.getCorrectCount());
        vo.setCorrectRate(question.getCorrectRate());
        
        return vo;
    }
    
    /**
     * 简单的 JSON 解析（根据实际需求，可以替换为 Jackson ObjectMapper）
     */
    private static Object parseJson(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        // 这里返回原始 JSON 字符串，由前端或后续处理解析
        return jsonString;
    }
}
