package com.yantiku.module.practice.controller;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.module.practice.model.dto.CreateSessionDTO;
import com.yantiku.module.practice.model.dto.SubmitAnswerDTO;
import com.yantiku.module.practice.model.vo.AnswerResultVO;
import com.yantiku.module.practice.model.vo.SessionVO;
import com.yantiku.module.practice.service.PracticeService;
import com.yantiku.common.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 练习控制器
 */
@RestController
@RequestMapping("/api/v1/practice")
@RequiredArgsConstructor
@Slf4j
public class PracticeController {

    private final PracticeService practiceService;

    /**
     * 创建练习会话
     * POST /api/v1/practice/sessions
     */
    @PostMapping("/sessions")
    public ApiResponse<SessionVO> createSession(
            @CurrentUser Long userId,
            @Validated @RequestBody CreateSessionDTO dto) {
        log.info("API请求: 创建练习会话, userId={}", userId);
        
        SessionVO session = practiceService.createSession(userId, dto);
        return ApiResponse.ok(session);
    }

    /**
     * 获取用户会话列表（分页）
     * GET /api/v1/practice/sessions
     */
    @GetMapping("/sessions")
    public ApiResponse<List<SessionVO>> getUserSessions(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("API请求: 获取用户会话列表, userId={}, offset={}, limit={}", userId, offset, limit);
        
        List<SessionVO> sessions = practiceService.getUserSessions(userId, offset, limit);
        return ApiResponse.ok(sessions);
    }

    /**
     * 获取会话详情
     * GET /api/v1/practice/sessions/{id}
     */
    @GetMapping("/sessions/{id}")
    public ApiResponse<SessionVO> getSession(@PathVariable Long id) {
        log.info("API请求: 获取会话详情, sessionId={}", id);
        
        SessionVO session = practiceService.getSession(id);
        return ApiResponse.ok(session);
    }

    /**
     * 提交答案
     * POST /api/v1/practice/sessions/{id}/answers
     */
    @PostMapping("/sessions/{id}/answers")
    public ApiResponse<AnswerResultVO> submitAnswer(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Validated @RequestBody SubmitAnswerDTO dto) {
        log.info("API请求: 提交答案, userId={}, sessionId={}, questionId={}", userId, id, dto.getQuestionId());
        
        AnswerResultVO result = practiceService.submitAnswer(userId, id, dto);
        return ApiResponse.ok(result);
    }

    /**
     * 暂停会话
     * PUT /api/v1/practice/sessions/{id}/pause
     */
    @PutMapping("/sessions/{id}/pause")
    public ApiResponse<Void> pauseSession(@PathVariable Long id) {
        log.info("API请求: 暂停会话, sessionId={}", id);
        
        practiceService.pauseSession(id);
        return ApiResponse.ok(null);
    }

    /**
     * 恢复会话
     * PUT /api/v1/practice/sessions/{id}/resume
     */
    @PutMapping("/sessions/{id}/resume")
    public ApiResponse<Void> resumeSession(@PathVariable Long id) {
        log.info("API请求: 恢复会话, sessionId={}", id);
        
        practiceService.resumeSession(id);
        return ApiResponse.ok(null);
    }

    /**
     * 完成会话
     * POST /api/v1/practice/sessions/{id}/complete
     */
    @PostMapping("/sessions/{id}/complete")
    public ApiResponse<SessionVO> completeSession(@PathVariable Long id) {
        log.info("API请求: 完成会话, sessionId={}", id);
        
        SessionVO session = practiceService.completeSession(id);
        return ApiResponse.ok(session);
    }

    /**
     * 放弃会话
     * DELETE /api/v1/practice/sessions/{id}
     */
    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> abandonSession(@PathVariable Long id) {
        log.info("API请求: 放弃会话, sessionId={}", id);
        
        practiceService.abandonSession(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/sessions/{id}/answers/batch")
    public ApiResponse<List<AnswerResultVO>> submitAnswersBatch(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @RequestBody List<SubmitAnswerDTO> dtos) {
        log.info("API请求: 批量提交答案, sessionId={}, count={}", id, dtos.size());
        return ApiResponse.ok(practiceService.submitAnswersBatch(userId, id, dtos));
    }

    /**
     * 获取下一批题目
     * GET /api/v1/practice/sessions/{id}/next-batch?offset=10
     */
    @GetMapping("/sessions/{id}/next-batch")
    public ApiResponse<List<com.yantiku.module.question.model.vo.QuestionVO>> getNextBatch(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int offset) {
        log.info("API请求: 获取下一批题目, sessionId={}, offset={}", id, offset);
        return ApiResponse.ok(practiceService.getNextBatch(id, offset));
    }

    /**
     * 获取用户在各科目下的掌握度
     * GET /api/v1/practice/mastery
     */
    @GetMapping("/mastery")
    public ApiResponse<List<Map<String, Object>>> getSubjectMastery(@CurrentUser Long userId) {
        log.info("API请求: 获取科目掌握度, userId={}", userId);
        return ApiResponse.ok(practiceService.getSubjectMastery(userId));
    }
}
