package com.yantiku.module.question.controller;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.common.dto.PageResult;
import com.yantiku.module.question.model.dto.QuestionSearchDTO;
import com.yantiku.module.question.model.vo.QuestionDetailVO;
import com.yantiku.module.question.model.vo.QuestionVO;
import com.yantiku.module.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

/**
 * 题目控制器
 * 
 * 提供题目的查询接口，包括搜索、详情、随机、热门等
 * 
 * 安全设计：
 * - 列表/搜索接口不返回答案
 * - 详情接口返回答案（用于评分）
 */
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 搜索题目（分页）
     * 
     * GET /api/v1/questions
     * 
     * @param directionId  方向ID（可选）
     * @param subjectId    科目ID（可选）
     * @param knowledgePointId 知识点ID（可选）
     * @param questionType 题目类型（可选）
     * @param difficulty   难度等级（可选）
     * @param examYear     考题年份（可选）
     * @param keyword      关键词（可选）
     * @param activeOnly   是否仅查询启用题目（默认 true）
     * @param page         页码（默认 1）
     * @param pageSize     每页数量（默认 20）
     * @return 分页结果（不包含答案）
     */
    @GetMapping
    public ApiResponse<PageResult<QuestionVO>> searchQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long knowledgePointId,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer examYear,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer pageSize
    ) {
        log.info("搜索题目请求");
        
        QuestionSearchDTO searchDTO = QuestionSearchDTO.builder()
                .subjectId(subjectId)
                .knowledgePointId(knowledgePointId)
                .questionType(questionType)
                .difficulty(difficulty)
                .examYear(examYear)
                .keyword(keyword)
                .activeOnly(activeOnly)
                .page(page)
                .pageSize(pageSize)
                .build();
        
        PageResult<QuestionVO> result = questionService.searchQuestions(searchDTO);
        
        return ApiResponse.ok(result);
    }

    /**
     * 根据 ID 获取题目（不包含答案）
     * 
     * GET /api/v1/questions/{id}
     * 
     * @param id 题目ID
     * @return 题目信息（不包含答案）
     */
    @GetMapping("/{id}")
    public ApiResponse<QuestionVO> getQuestionById(@PathVariable Long id) {
        log.info("根据 ID 获取题目请求，id={}", id);
        
        QuestionVO question = questionService.getQuestionById(id);
        
        return ApiResponse.ok(question);
    }

    /**
     * 获取随机题目
     * 
     * GET /api/v1/questions/random
     * 
     * @param directionId  方向ID（可选）
     * @param subjectId    科目ID（可选）
     * @param questionType 题目类型（可选）
     * @param count        获取数量（默认 10）
     * @return 随机题目列表（不包含答案）
     */
    @GetMapping("/random")
    public ApiResponse<List<QuestionVO>> getRandomQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(50) Integer count
    ) {
        log.info("获取随机题目请求");
        
        List<QuestionVO> questions = questionService.getRandomQuestions(
                subjectId, questionType, count);
        
        return ApiResponse.ok(questions);
    }

    /**
     * 获取热门题目
     * 
     * GET /api/v1/questions/hot
     * 
     * @param limit 获取数量（默认 20）
     * @return 热门题目列表（不包含答案）
     */
    @GetMapping("/hot")
    public ApiResponse<List<QuestionVO>> getHotQuestions(
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer limit
    ) {
        log.info("获取热门题目请求");
        
        List<QuestionVO> questions = questionService.getHotQuestions(limit);
        
        return ApiResponse.ok(questions);
    }

    @GetMapping("/search")
    public ApiResponse<PageResult<QuestionVO>> fullTextSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer pageSize
    ) {
        log.info("full text search: keyword={}", keyword);
        QuestionSearchDTO searchDTO = QuestionSearchDTO.builder()
                .keyword(keyword).page(page).pageSize(pageSize).build();
        return ApiResponse.ok(questionService.searchQuestions(searchDTO));
    }

    @GetMapping("/{id}/similar")
    public ApiResponse<List<QuestionVO>> getSimilarQuestions(@PathVariable Long id) {
        return ApiResponse.ok(List.of());
    }

    @PostMapping("/{id}/report")
    public ApiResponse<Void> reportQuestion(@PathVariable Long id, @RequestBody Map<String, String> body) {
        log.info("report question: id={}", id);
        return ApiResponse.ok();
    }
}
