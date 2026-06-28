package com.yantiku.module.question.controller;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.module.question.model.vo.DirectionVO;
import com.yantiku.module.question.model.vo.KnowledgePointVO;
import com.yantiku.module.question.model.vo.SubjectVO;
import com.yantiku.module.question.service.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 考试方向控制器
 * 
 * 提供方向、科目、知识点的查询接口，
 * 支持树形结构查询
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DirectionController {

    private final DirectionService directionService;

    /**
     * 获取所有方向（包含科目和知识点树）
     * 
     * GET /api/v1/directions
     * 
     * @return 方向列表（完整树形结构）
     */
    @GetMapping("/directions")
    public ApiResponse<List<DirectionVO>> getAllDirections() {
        log.info("获取所有方向请求");
        
        List<DirectionVO> directions = directionService.getAllDirections();
        
        return ApiResponse.ok(directions);
    }

    /**
     * 根据 ID 获取方向（包含科目）
     * 
     * GET /api/v1/directions/{id}
     * 
     * @param id 方向ID
     * @return 方向信息（包含科目列表）
     */
    @GetMapping("/directions/{id}")
    public ApiResponse<DirectionVO> getDirectionById(@PathVariable Long id) {
        log.info("根据 ID 获取方向请求，id={}", id);
        
        DirectionVO direction = directionService.getDirectionById(id);
        
        return ApiResponse.ok(direction);
    }

    /**
     * 根据方向 ID 获取科目列表
     * 
     * GET /api/v1/subjects?directionId=1
     * 
     * @param directionId 方向ID
     * @return 科目列表（包含知识点）
     */
    @GetMapping("/subjects")
    public ApiResponse<List<SubjectVO>> getSubjectsByDirectionId(
            @RequestParam(required = false) Long directionId
    ) {
        log.info("根据方向 ID 获取科目列表请求，directionId={}", directionId);
        
        List<SubjectVO> subjects = directionService.getSubjectsByDirectionId(directionId);
        
        return ApiResponse.ok(subjects);
    }

    /**
     * 根据 ID 获取科目（包含知识点树）
     * 
     * GET /api/v1/subjects/{id}
     * 
     * @param id 科目ID
     * @return 科目信息（包含知识点树）
     */
    @GetMapping("/subjects/{id}")
    public ApiResponse<SubjectVO> getSubjectById(@PathVariable Long id) {
        log.info("根据 ID 获取科目请求，id={}", id);
        
        SubjectVO subject = directionService.getSubjectById(id);
        
        return ApiResponse.ok(subject);
    }

    /**
     * 根据科目 ID 获取知识点列表（扁平结构）
     * 
     * GET /api/v1/knowledge-points?subjectId=1
     * 
     * @param subjectId 科目ID
     * @return 知识点列表
     */
    @GetMapping("/knowledge-points")
    public ApiResponse<List<KnowledgePointVO>> getKnowledgePointsBySubjectId(
            @RequestParam(required = false) Long subjectId
    ) {
        log.info("根据科目 ID 获取知识点列表请求，subjectId={}", subjectId);
        
        List<KnowledgePointVO> knowledgePoints = directionService.getKnowledgePointsBySubjectId(subjectId);
        
        return ApiResponse.ok(knowledgePoints);
    }

    /**
     * 根据科目 ID 获取知识点树（树形结构）
     * 
     * GET /api/v1/knowledge-points/tree?subjectId=1
     * 
     * @param subjectId 科目ID
     * @return 知识点树（仅根节点，子节点在 children 字段）
     */
    @GetMapping("/knowledge-points/tree")
    public ApiResponse<List<KnowledgePointVO>> getKnowledgePointTree(
            @RequestParam Long subjectId
    ) {
        log.info("根据科目 ID 获取知识点树请求，subjectId={}", subjectId);
        
        List<KnowledgePointVO> tree = directionService.getKnowledgePointTreeBySubjectId(subjectId);
        
        return ApiResponse.ok(tree);
    }
}
