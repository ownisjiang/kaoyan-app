package com.yantiku.module.grading.controller;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.module.grading.model.dto.ComprehensiveSubmitDTO;
import com.yantiku.module.grading.model.vo.GradingTaskVO;
import com.yantiku.module.grading.service.GradingService;
import com.yantiku.module.question.mapper.QuestionMapper;
import com.yantiku.module.question.model.entity.Question;
import com.yantiku.common.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grading")
@RequiredArgsConstructor
@Slf4j
public class GradingController {

    private final GradingService gradingService;
    private final QuestionMapper questionMapper;

    @PostMapping("/choice")
    public ApiResponse<?> gradeChoice(@RequestParam Long questionId,
                                       @RequestParam String userAnswer) {
        Question question = questionMapper.findById(questionId);
        if (question == null) return ApiResponse.fail(404, "question not found");
        if (question.getQuestionType() != 1) return ApiResponse.fail(400, "not a choice question");
        return ApiResponse.ok(gradingService.gradeChoice(userAnswer, question));
    }

    @PostMapping("/fill-blank")
    public ApiResponse<?> gradeFillBlank(@RequestParam Long questionId,
                                          @RequestParam String userAnswer) {
        Question question = questionMapper.findById(questionId);
        if (question == null) return ApiResponse.fail(404, "question not found");
        if (question.getQuestionType() != 2) return ApiResponse.fail(400, "not a fill-blank question");
        return ApiResponse.ok(gradingService.gradeFillBlank(userAnswer, question));
    }

    @PostMapping("/comprehensive")
    public ApiResponse<GradingTaskVO> submitComprehensive(
            @CurrentUser Long userId,
            @Validated @RequestBody ComprehensiveSubmitDTO dto) {
        return ApiResponse.ok(gradingService.submitComprehensive(userId, dto));
    }

    @GetMapping("/comprehensive/{taskId}")
    public ApiResponse<?> getComprehensiveResult(@PathVariable String taskId) {
        return gradingService.getComprehensiveResult(taskId);
    }

    @GetMapping("/tasks")
    public ApiResponse<List<GradingTaskVO>> getTasks(@CurrentUser Long userId) {
        return ApiResponse.ok(List.of());
    }
}
