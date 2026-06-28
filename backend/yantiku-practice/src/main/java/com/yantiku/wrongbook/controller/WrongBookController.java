package com.yantiku.module.wrongbook.controller;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.common.dto.PageResult;
import com.yantiku.module.wrongbook.mapper.WrongQuestionBookMapper.WrongQuestionStats;
import com.yantiku.module.wrongbook.model.vo.WrongQuestionVO;
import com.yantiku.module.wrongbook.service.WrongBookService;
import com.yantiku.common.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 错题本控制器
 *
 * 提供错题查询、统计、复习、批量复习、移出等接口
 */
@RestController
@RequestMapping("/api/v1/wrong-book")
@RequiredArgsConstructor
@Slf4j
public class WrongBookController {

    private final WrongBookService wrongBookService;

    /**
     * 查询错题本（游标分页）
     *
     * GET /api/v1/wrong-book?cursor=0&size=20
     *
     * @param userId 当前用户 ID
     * @param cursor 游标（上一页最后一条记录 ID，首页传 0 或不传）
     * @param size   每页数量（默认 20）
     * @return 错题列表
     */
    @GetMapping
    public ApiResponse<PageResult<WrongQuestionVO>> getWrongBook(
            @CurrentUser Long userId,
            @RequestParam(required = false, defaultValue = "0") Long cursor,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        log.info("【查询错题本】userId={}, cursor={}, size={}", userId, cursor, size);
        PageResult<WrongQuestionVO> result = wrongBookService.getWrongBook(userId, cursor, size);
        return ApiResponse.ok(result);
    }

    /**
     * 获取错题统计
     *
     * GET /api/v1/wrong-book/stats
     *
     * @param userId 当前用户 ID
     * @return 统计结果（总错题数 + 待复习数）
     */
    @GetMapping("/stats")
    public ApiResponse<WrongQuestionStats> getWrongBookStats(@CurrentUser Long userId) {
        log.info("【获取错题统计】userId={}", userId);
        WrongQuestionStats stats = wrongBookService.getWrongBookStats(userId);
        return ApiResponse.ok(stats);
    }

    /**
     * 标记错题为已复习
     *
     * POST /api/v1/wrong-book/{wrongBookId}/review
     *
     * @param userId       当前用户 ID
     * @param wrongBookId  错题记录 ID
     * @return 操作结果
     */
    @PostMapping("/{wrongBookId}/review")
    public ApiResponse<Void> markReviewed(
            @CurrentUser Long userId,
            @PathVariable Long wrongBookId
    ) {
        log.info("【标记复习】userId={}, wrongBookId={}", userId, wrongBookId);
        wrongBookService.markReviewed(userId, wrongBookId);
        return ApiResponse.ok(null);
    }

    /**
     * 批量标记复习
     *
     * POST /api/v1/wrong-book/batch-review
     *
     * @param userId 当前用户 ID
     * @param ids    错题记录 ID 列表
     * @return 操作结果
     */
    @PostMapping("/batch-review")
    public ApiResponse<Void> batchReview(
            @CurrentUser Long userId,
            @RequestBody List<Long> ids
    ) {
        log.info("【批量标记复习】userId={}, ids={}", userId, ids);
        wrongBookService.batchReview(userId, ids);
        return ApiResponse.ok(null);
    }

    /**
     * 将题目移出错题本（标记为已掌握）
     *
     * DELETE /api/v1/wrong-book/{questionId}
     *
     * @param userId     当前用户 ID
     * @param questionId 题目 ID
     * @return 操作结果
     */
    @DeleteMapping("/{questionId}")
    public ApiResponse<Void> removeFromWrongBook(
            @CurrentUser Long userId,
            @PathVariable Long questionId
    ) {
        log.info("【移出错题本】userId={}, questionId={}", userId, questionId);
        wrongBookService.removeFromWrongBook(userId, questionId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/generate-paper")
    public ApiResponse<Map<String, Object>> generatePaper(
            @CurrentUser Long userId,
            @RequestBody Map<String, Object> params) {
        log.info("generate paper: userId={}", userId);
        // 从错题本中随机选取题目创建刷题会话
        return ApiResponse.fail(501, "组卷功能开发中，请直接在错题本中选择题目练习");
    }

    /**
     * 按科目→题型分组获取错题
     */
    @GetMapping("/grouped")
    public ApiResponse<List<Map<String, Object>>> getGroupedWrongBook(@CurrentUser Long userId) {
        log.info("【分组错题本】userId={}", userId);
        return ApiResponse.ok(wrongBookService.getGroupedWrongBook(userId));
    }
}
