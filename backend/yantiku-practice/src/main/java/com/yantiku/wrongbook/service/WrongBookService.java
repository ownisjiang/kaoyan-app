package com.yantiku.module.wrongbook.service;

import com.yantiku.common.dto.PageResult;
import com.yantiku.common.exception.NotFoundException;
import com.yantiku.module.question.mapper.QuestionMapper;
import com.yantiku.module.question.mapper.SubjectMapper;
import com.yantiku.module.question.model.entity.Question;
import com.yantiku.module.question.model.entity.Subject;
import com.yantiku.module.wrongbook.mapper.WrongQuestionBookMapper;
import com.yantiku.module.wrongbook.mapper.WrongQuestionBookMapper.WrongQuestionStats;
import com.yantiku.module.wrongbook.model.entity.WrongQuestionBook;
import com.yantiku.module.wrongbook.model.vo.WrongQuestionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 错题本服务
 *
 * 提供错题查询（游标分页）、统计、间隔复习、批量复习、移出错题本等功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WrongBookService {

    private final WrongQuestionBookMapper wrongQuestionBookMapper;
    private final QuestionMapper questionMapper;
    private final SubjectMapper subjectMapper;

    /** 间隔重复：各掌握等级对应的复习间隔天数 */
    private static final int[] REVIEW_INTERVALS = {1, 3, 7, 14, 30};

    /**
     * 游标分页查询用户错题本
     *
     * @param userId 用户 ID
     * @param cursor 游标（上一页最后一条记录 ID，首页传 null 或 0）
     * @param size   每页数量（默认 20）
     * @return 分页结果
     */
    public PageResult<WrongQuestionVO> getWrongBook(Long userId, Long cursor, int size) {
        log.info("查询错题本: userId={}, cursor={}, size={}", userId, cursor, size);

        // cursor <= 0 视为首页
        Long effectiveCursor = (cursor != null && cursor > 0) ? cursor : null;

        List<WrongQuestionBook> records = wrongQuestionBookMapper.findByUserIdCursor(userId, effectiveCursor, size);
        List<WrongQuestionVO> voList = records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 使用统计接口获取总数
        WrongQuestionStats stats = wrongQuestionBookMapper.selectStats(userId);
        long total = stats != null ? stats.getTotalWrong() : 0;

        return PageResult.of(voList, total, 1, size);
    }

    /**
     * 获取用户错题统计
     *
     * @param userId 用户 ID
     * @return 统计结果（总错题数 + 待复习数）
     */
    public WrongQuestionStats getWrongBookStats(Long userId) {
        log.info("获取错题统计: userId={}", userId);
        return wrongQuestionBookMapper.selectStats(userId);
    }

    /**
     * 按科目→题型分组获取错题列表
     */
    public List<Map<String, Object>> getGroupedWrongBook(Long userId) {
        List<WrongQuestionBook> all = wrongQuestionBookMapper.findByUserId(userId);
        Map<String, List<WrongQuestionVO>> bySubject = new LinkedHashMap<>();
        
        Map<Integer, String> typeLabels = Map.of(1, "选择题", 2, "填空题", 3, "综合题");
        
        for (WrongQuestionBook wqb : all) {
            WrongQuestionVO vo = convertToVO(wqb);
            String key = vo.getKnowledgePointName() + "|" + wqb.getSubjectId();
            bySubject.computeIfAbsent(key, k -> new ArrayList<>()).add(vo);
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<WrongQuestionVO>> entry : bySubject.entrySet()) {
            List<WrongQuestionVO> items = entry.getValue();
            
            // 按题型分组
            Map<Integer, List<WrongQuestionVO>> byType = new LinkedHashMap<>();
            for (WrongQuestionVO vo : items) {
                int qt = vo.getQuestionType() != null ? vo.getQuestionType() : 1;
                byType.computeIfAbsent(qt, k -> new ArrayList<>()).add(vo);
            }
            
            List<Map<String, Object>> typeGroups = new ArrayList<>();
            for (Map.Entry<Integer, List<WrongQuestionVO>> te : byType.entrySet()) {
                Map<String, Object> tg = new LinkedHashMap<>();
                tg.put("type", te.getKey());
                tg.put("label", typeLabels.getOrDefault(te.getKey(), "未知"));
                tg.put("count", te.getValue().size());
                tg.put("questions", te.getValue());
                typeGroups.add(tg);
            }
            
            WrongQuestionVO first = items.get(0);
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("subject", first.getKnowledgePointName());
            group.put("subjectId", items.get(0).getQuestionId()); // approximate
            group.put("total", items.size());
            group.put("types", typeGroups);
            result.add(group);
        }
        return result;
    }

    /**
     * 标记错题为已复习
     * <p>
     * 根据当前掌握等级（masteryLevel）计算下次复习时间：
     * 0→1天, 1→3天, 2→7天, 3→14天, 4→30天
     *
     * @param userId       用户 ID
     * @param wrongBookId  错题记录 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markReviewed(Long userId, Long wrongBookId) {
        log.info("标记复习: userId={}, wrongBookId={}", userId, wrongBookId);

        WrongQuestionBook record = wrongQuestionBookMapper.findById(wrongBookId);
        if (record == null) {
            throw new NotFoundException("错题记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new NotFoundException("错题记录不存在");
        }

        int masteryLevel = record.getMasteryLevel() != null ? record.getMasteryLevel() : 0;
        int intervalDays = masteryLevel < REVIEW_INTERVALS.length
                ? REVIEW_INTERVALS[masteryLevel]
                : REVIEW_INTERVALS[REVIEW_INTERVALS.length - 1];

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReviewAt = now.plusDays(intervalDays);

        wrongQuestionBookMapper.updateReview(wrongBookId, now, nextReviewAt);
        log.info("复习标记成功: wrongBookId={}, nextReviewAt={}", wrongBookId, nextReviewAt);
    }

    /**
     * 批量标记复习
     *
     * @param userId 用户 ID
     * @param ids    错题记录 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchReview(Long userId, List<Long> ids) {
        log.info("批量标记复习: userId={}, ids={}", userId, ids);
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            markReviewed(userId, id);
        }
    }

    /**
     * 将题目从错题本移除（标记为已掌握）
     *
     * @param userId     用户 ID
     * @param questionId 题目 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeFromWrongBook(Long userId, Long questionId) {
        log.info("移出错题本: userId={}, questionId={}", userId, questionId);

        WrongQuestionBook record = wrongQuestionBookMapper.findByUserIdAndQuestionId(userId, questionId);
        if (record == null) {
            throw new NotFoundException("错题记录不存在");
        }

        wrongQuestionBookMapper.updateStatus(record.getId(), "mastered");
        log.info("移出错题本成功: wrongBookId={}, questionId={}", record.getId(), questionId);
    }

    // ========================================================
    // 私有方法
    // ========================================================

    /**
     * 将错题实体转换为 VO，补充题目和知识点信息
     */
    private WrongQuestionVO convertToVO(WrongQuestionBook record) {
        WrongQuestionVO.WrongQuestionVOBuilder builder = WrongQuestionVO.builder()
                .id(record.getId())
                .questionId(record.getQuestionId())
                .knowledgePointId(record.getSubjectId())
                .wrongCount(record.getWrongCount() != null ? record.getWrongCount() : 0)
                .consecutiveCorrect(record.getConsecutiveCorrect() != null ? record.getConsecutiveCorrect() : 0)
                .lastWrongAt(record.getLastWrongAt())
                .lastReviewedAt(record.getLastReviewedAt())
                .nextReviewAt(record.getNextReviewAt())
                .masteryLevel(record.getMasteryLevel() != null ? record.getMasteryLevel() : 0)
                .status(record.getStatus());

        // 补充科目名称
        if (record.getSubjectId() != null) {
            try {
                Subject subj = subjectMapper.findById(record.getSubjectId());
                if (subj != null) {
                    builder.knowledgePointName(subj.getName());
                }
            } catch (Exception e) {
                log.warn("查询科目失败: subjectId={}, error={}", record.getSubjectId(), e.getMessage());
            }
        }

        // 补充题目简要信息
        if (record.getQuestionId() != null) {
            try {
                Question question = questionMapper.findById(record.getQuestionId());
                if (question != null) {
                    builder.questionType(question.getQuestionType())
                            .difficulty(question.getDifficulty())
                            .contentPreview(getContentPreview(question.getContent()));
                }
            } catch (Exception e) {
                log.warn("查询题目失败: questionId={}, error={}", record.getQuestionId(), e.getMessage());
            }
        }

        return builder.build();
    }

    /**
     * 提取题目内容预览（前 100 字符）
     */
    private String getContentPreview(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        return content.length() > 100 ? content.substring(0, 100) : content;
    }
}
