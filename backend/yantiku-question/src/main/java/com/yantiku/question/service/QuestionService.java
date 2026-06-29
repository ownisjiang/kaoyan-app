package com.yantiku.module.question.service;

import com.yantiku.common.dto.PageResult;
import com.yantiku.module.question.mapper.QuestionMapper;
import com.yantiku.module.question.model.entity.Question;
import com.yantiku.module.question.model.dto.CreateQuestionDTO;
import com.yantiku.module.question.model.dto.QuestionSearchDTO;
import com.yantiku.module.question.model.vo.QuestionDetailVO;
import com.yantiku.module.question.model.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目服务类
 * 
 * 处理题目的业务逻辑，包括搜索、查询、随机获取、热门题目等
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionMapper questionMapper;

    /**
     * 搜索题目（分页）
     * 
     * 用于列表查询，不返回答案（安全考虑）
     * 
     * @param dto 搜索条件 DTO
     * @return 分页结果
     */
    public PageResult<QuestionVO> searchQuestions(QuestionSearchDTO dto) {
        log.info("搜索题目，条件：{}", dto);
        
        // 查询总数
        long total = questionMapper.countQuestions(dto);
        
        // 查询列表
        List<Question> questions = questionMapper.searchQuestions(dto);
        
        // 转换为 VO（不包含答案）
        List<QuestionVO> voList = questions.stream()
                .map(QuestionVO::fromEntity)
                .collect(Collectors.toList());
        
        // 构建分页结果
        return PageResult.of(voList, total, dto.getPage(), dto.getPageSize());
    }

    /**
     * 根据 ID 获取题目（不包含答案）
     * 
     * 用于列表、搜索等场景，不暴露答案
     * 
     * @param id 题目ID
     * @return 题目视图对象（无答案）
     * @throws com.yantiku.common.exception.NotFoundException 题目不存在
     */
    public QuestionVO getQuestionById(Long id) {
        log.info("根据 ID 获取题目（无答案），id={}", id);
        
        Question question = questionMapper.findById(id);
        if (question == null) {
            throw new com.yantiku.common.exception.NotFoundException("题目不存在");
        }
        
        return QuestionVO.fromEntity(question);
    }

    /**
     * 根据 ID 获取题目详情（包含答案）
     * 
     * 用于评分场景，返回完整信息包括答案
     * 
     * @param id 题目ID
     * @return 题目详细视图对象（含答案）
     * @throws com.yantiku.common.exception.NotFoundException 题目不存在
     */
    public QuestionDetailVO getQuestionDetail(Long id) {
        log.info("根据 ID 获取题目详情（含答案），id={}", id);
        
        Question question = questionMapper.findById(id);
        if (question == null) {
            throw new com.yantiku.common.exception.NotFoundException("题目不存在");
        }
        
        return QuestionDetailVO.fromEntity(question);
    }

    /**
     * 随机获取题目
     * 
     * 用于每日一练、随机练习等场景
     * 
     * @param directionId  方向ID（可选）
     * @param subjectId    科目ID（可选）
     * @param questionType 题目类型（可选）
     * @param count        获取数量
     * @return 题目列表（不包含答案）
     */
    public List<QuestionVO> getRandomQuestions(Long subjectId, Integer questionType, int count) {
        log.info("随机获取题目，subjectId={}, questionType={}, count={}", 
                subjectId, questionType, count);
        
        List<Question> questions = questionMapper.findRandom(subjectId, questionType, count);
        
        return questions.stream()
                .map(QuestionVO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门题目
     * 
     * 按使用次数降序排列
     * 
     * @param limit 获取数量
     * @return 题目列表（不包含答案）
     */
    public List<QuestionVO> getHotQuestions(int limit) {
        log.info("获取热门题目，limit={}", limit);
        
        List<Question> questions = questionMapper.findHot(limit);
        
        return questions.stream()
                .map(QuestionVO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== 管理员操作 ====================

    public Long createQuestion(Long userId, CreateQuestionDTO dto) {
        Question q = Question.builder()
                .subjectId(dto.getSubjectId())
                .knowledgePointId(dto.getKnowledgePointId())
                .questionType(dto.getQuestionType())
                .difficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 1)
                .examYear(dto.getExamYear())
                .source(dto.getSource())
                .content(dto.getContent())
                .options(dto.getOptions())
                .answer(dto.getAnswer())
                .analysis(dto.getAnalysis())
                .tags(dto.getTags())
                .createdBy(userId)
                .isActive(true)
                .useCount(0)
                .correctCount(0)
                .build();
        questionMapper.insert(q);
        return q.getId();
    }

    public void updateQuestion(Long id, CreateQuestionDTO dto) {
        Question q = questionMapper.findById(id);
        if (q == null) throw new RuntimeException("题目不存在");
        q.setSubjectId(dto.getSubjectId());
        q.setKnowledgePointId(dto.getKnowledgePointId());
        q.setQuestionType(dto.getQuestionType());
        q.setDifficulty(dto.getDifficulty());
        q.setExamYear(dto.getExamYear());
        q.setSource(dto.getSource());
        q.setContent(dto.getContent());
        q.setOptions(dto.getOptions());
        q.setAnswer(dto.getAnswer());
        q.setAnalysis(dto.getAnalysis());
        q.setTags(dto.getTags());
        questionMapper.update(q);
    }

    public void deleteQuestion(Long id) {
        Question q = questionMapper.findById(id);
        if (q == null) throw new RuntimeException("题目不存在");
        q.setIsActive(false);
        questionMapper.update(q);
    }
}
