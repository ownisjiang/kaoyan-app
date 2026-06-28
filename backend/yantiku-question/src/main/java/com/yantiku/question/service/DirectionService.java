package com.yantiku.module.question.service;

import com.yantiku.module.question.mapper.DirectionMapper;
import com.yantiku.module.question.mapper.KnowledgePointMapper;
import com.yantiku.module.question.mapper.QuestionMapper;
import com.yantiku.module.question.mapper.SubjectMapper;
import com.yantiku.module.question.model.entity.Direction;
import com.yantiku.module.question.model.entity.KnowledgePoint;
import com.yantiku.module.question.model.entity.Subject;
import com.yantiku.module.question.model.vo.DirectionVO;
import com.yantiku.module.question.model.vo.KnowledgePointVO;
import com.yantiku.module.question.model.vo.SubjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectionService {

    private final DirectionMapper directionMapper;
    private final SubjectMapper subjectMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final QuestionMapper questionMapper;

    public List<DirectionVO> getAllDirections() {
        return directionMapper.findActive().stream()
                .map(d -> DirectionVO.builder()
                        .id(d.getId()).code(d.getCode()).name(d.getName()).build())
                .collect(Collectors.toList());
    }

    public DirectionVO getDirectionById(Long id) {
        Direction d = directionMapper.findById(id);
        if (d == null) throw new com.yantiku.common.exception.NotFoundException("direction not found");
        return DirectionVO.builder()
                .id(d.getId()).code(d.getCode()).name(d.getName()).build();
    }

    @Cacheable(value = "subject_list", key = "#directionId != null ? #directionId : 'all'", unless = "#result == null || #result.isEmpty()")
    public List<SubjectVO> getSubjectsByDirectionId(Long directionId) {
        List<Subject> subjects;
        if (directionId != null) {
            subjects = subjectMapper.findByDirectionId(directionId);
        } else {
            subjects = subjectMapper.findAll();
        }

        // 1. 查询所有科目下的题型分布统计（subject_id → type → count）
        List<Map<String, Object>> typeCounts = questionMapper.countGroupBySubjectAndType();
        Map<Long, List<SubjectVO.QuestionTypeCount>> typeCountMap = typeCounts.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row.get("subject_id")).longValue(),
                        Collectors.mapping(
                                row -> SubjectVO.QuestionTypeCount.builder()
                                        .type(String.valueOf(row.get("type")))
                                        .label(typeToLabel(String.valueOf(row.get("type"))))
                                        .count(((Number) row.get("cnt")).intValue())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        // 掌握度由 yantiku-practice 服务的统计接口提供，此处设为 0
        return subjects.stream()
                .map(s -> {
                    int total = questionMapper.countActiveBySubject(s.getId());
                    return SubjectVO.builder()
                            .id(s.getId())
                            .directionId(s.getDirectionId())
                            .name(s.getName())
                            .code(s.getCode())
                            .sortOrder(s.getSort())
                            .mastery(0)
                            .questionTypes(typeCountMap.getOrDefault(s.getId(), List.of()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    public SubjectVO getSubjectById(Long id) {
        Subject s = subjectMapper.findById(id);
        if (s == null) throw new com.yantiku.common.exception.NotFoundException("subject not found");
        
        // 按题型统计
        List<Map<String, Object>> typeCounts = questionMapper.countBySubjectAndType(id);
        List<SubjectVO.QuestionTypeCount> typeList = new ArrayList<>();
        Map<Integer, String> labels = Map.of(1, "选择题", 2, "填空题", 3, "综合题");
        for (Map<String, Object> row : typeCounts) {
            int qtype = ((Number) row.get("type")).intValue();
            int cnt = ((Number) row.get("cnt")).intValue();
            typeList.add(new SubjectVO.QuestionTypeCount(
                    String.valueOf(qtype), labels.getOrDefault(qtype, "未知"), cnt));
        }
        
        return SubjectVO.builder()
                .id(s.getId()).directionId(s.getDirectionId()).name(s.getName())
                .code(s.getCode()).sortOrder(s.getSort())
                .mastery(0)
                .questionTypes(typeList)
                .build();
    }

    public List<KnowledgePointVO> getKnowledgePointsBySubjectId(Long subjectId) {
        return knowledgePointMapper.findBySubjectId(subjectId).stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    public List<KnowledgePointVO> getKnowledgePointTreeBySubjectId(Long subjectId) {
        return buildTree(knowledgePointMapper.findBySubjectId(subjectId));
    }

    private List<KnowledgePointVO> buildTree(List<KnowledgePoint> list) {
        Map<Long, KnowledgePointVO> map = list.stream().map(this::toVO)
                .collect(Collectors.toMap(KnowledgePointVO::getId, v -> v));
        List<KnowledgePointVO> roots = new ArrayList<>();
        for (KnowledgePointVO vo : map.values()) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            } else {
                KnowledgePointVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                    parent.getChildren().add(vo);
                }
            }
        }
        return roots;
    }

    private KnowledgePointVO toVO(KnowledgePoint kp) {
        KnowledgePointVO vo = new KnowledgePointVO();
        vo.setId(kp.getId());
        vo.setSubjectId(kp.getSubjectId());
        vo.setParentId(kp.getParentId());
        vo.setName(kp.getName());
        vo.setLevel(kp.getLevel());
        vo.setSortOrder(kp.getSort());
        return vo;
    }

    /**
     * 题目类型编码 → 中文标签
     */
    private static String typeToLabel(String type) {
        switch (type) {
            case "1": return "选择题";
            case "2": return "填空题";
            case "3": return "综合题";
            default:  return "未知";
        }
    }
}
