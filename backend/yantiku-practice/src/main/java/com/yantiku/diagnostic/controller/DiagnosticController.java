package com.yantiku.module.diagnostic.controller;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.module.diagnostic.model.vo.DiagnosticReportVO;
import com.yantiku.module.diagnostic.service.DiagnosticService;
import com.yantiku.common.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 诊断报告控制器
 *
 * 提供诊断报告查询、生成等接口
 */
@RestController
@RequestMapping("/api/v1/diagnostic")
@RequiredArgsConstructor
@Slf4j
public class DiagnosticController {

    private final DiagnosticService diagnosticService;

    /**
     * 查询用户诊断报告列表（分页）
     *
     * GET /api/v1/diagnostic/reports?offset=0&limit=10
     *
     * @param userId 当前用户 ID
     * @param offset 偏移量（默认 0）
     * @param limit  每页数量（默认 10）
     * @return 报告列表
     */
    @GetMapping("/reports")
    public ApiResponse<List<DiagnosticReportVO>> getReports(
            @CurrentUser Long userId,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        log.info("【查询诊断报告列表】userId={}, offset={}, limit={}", userId, offset, limit);
        List<DiagnosticReportVO> reports = diagnosticService.getReports(userId, offset, limit);
        return ApiResponse.ok(reports);
    }

    /**
     * 获取最新诊断报告
     */
    @GetMapping("/reports/latest")
    public ApiResponse<DiagnosticReportVO> getLatestReport(@CurrentUser Long userId) {
        log.info("【获取最新诊断报告】userId={}", userId);
        DiagnosticReportVO report = diagnosticService.getLatestReport(userId);
        return report != null ? ApiResponse.ok(report) : ApiResponse.ok(null);
    }

    /**
     * 根据 ID 查询诊断报告
     *
     * GET /api/v1/diagnostic/reports/{id}
     *
     * @param id 报告 ID
     * @return 报告详情
     */
    @GetMapping("/reports/{id}")
    public ApiResponse<DiagnosticReportVO> getReport(@PathVariable Long id) {
        log.info("【查询诊断报告】reportId={}", id);
        DiagnosticReportVO report = diagnosticService.getReport(id);
        return ApiResponse.ok(report);
    }

    /**
     * 生成诊断报告
     *
     * POST /api/v1/diagnostic/generate?sessionId=123
     *
     * @param userId    当前用户 ID
     * @param sessionId 练习会话 ID
     * @return 生成的报告
     */
    @PostMapping("/generate")
    public ApiResponse<DiagnosticReportVO> generateReport(
            @CurrentUser Long userId,
            @RequestParam Long sessionId
    ) {
        log.info("【生成诊断报告】userId={}, sessionId={}", userId, sessionId);
        DiagnosticReportVO report = diagnosticService.generateReport(userId, sessionId);
        return ApiResponse.ok(report);
    }
}
