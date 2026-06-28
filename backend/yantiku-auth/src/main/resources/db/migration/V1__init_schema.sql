-- ============================================================
-- 研题库数据库 — 完整表结构
-- MySQL 8.0
-- 设计原则：科目（subjects）为题目组织的一级维度
-- ============================================================

-- ========================================
-- 用户表
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    avatar_url VARCHAR(500),
    target_direction_id BIGINT,
    target_school VARCHAR(200),
    exam_year INT,
    role VARCHAR(20) DEFAULT 'student',
    status VARCHAR(20) DEFAULT 'active',
    total_questions INT DEFAULT 0,
    total_correct INT DEFAULT 0,
    total_duration_sec BIGINT DEFAULT 0,
    streak_days INT DEFAULT 0,
    last_active_date DATE NULL,
    last_login_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_phone (phone),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 刷新令牌表
-- ========================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    family VARCHAR(64),
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_refresh_tokens_user (user_id),
    INDEX idx_refresh_tokens_hash (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 考试方向表（轻量分类标签）
-- ========================================
CREATE TABLE IF NOT EXISTS directions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20),
    sort INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 科目表（题库核心组织维度）
-- ========================================
CREATE TABLE IF NOT EXISTS subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    direction_id BIGINT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20),
    sort INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_subjects_direction (direction_id),
    INDEX idx_subjects_active (is_active, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 知识点表
-- ========================================
CREATE TABLE IF NOT EXISTS knowledge_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(200) NOT NULL,
    level INT DEFAULT 1,
    sort INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_knowledge_points_subject (subject_id),
    INDEX idx_knowledge_points_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 题目表（仅关联科目和知识点，不直接关联方向）
-- ========================================
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type INT NOT NULL COMMENT '1=选择题 2=填空题 3=综合题',
    difficulty INT DEFAULT 1 COMMENT '1=简单 2=中等 3=困难',
    subject_id BIGINT NOT NULL COMMENT '所属科目',
    knowledge_point_id BIGINT COMMENT '关联知识点',
    content TEXT NOT NULL COMMENT '题目内容',
    options JSON COMMENT '选项（选择题才有）',
    answer TEXT COMMENT '参考答案',
    explanation TEXT COMMENT '解析',
    source VARCHAR(200) COMMENT '题目来源',
    year INT COMMENT '真题年份',
    use_count INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_questions_subject (subject_id),
    INDEX idx_questions_kp (knowledge_point_id),
    INDEX idx_questions_type_subject (type, subject_id),
    INDEX idx_questions_active (is_active, subject_id, type),
    FULLTEXT INDEX idx_questions_content (content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 用户答题记录表
-- ========================================
CREATE TABLE IF NOT EXISTS user_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    user_answer TEXT COMMENT '用户提交的答案',
    is_correct TINYINT(1),
    score DECIMAL(5,2),
    max_score DECIMAL(5,2),
    time_spent INT DEFAULT 0 COMMENT '耗时（秒）',
    is_collected TINYINT(1) DEFAULT 0,
    ai_feedback TEXT COMMENT 'AI反馈JSON',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_answers_session (session_id),
    INDEX idx_user_answers_user (user_id),
    INDEX idx_user_answers_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 刷题会话表
-- ========================================
CREATE TABLE IF NOT EXISTS practice_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subject_id BIGINT COMMENT '刷题科目',
    mode VARCHAR(20) DEFAULT 'quick_quiz' COMMENT 'quick_quiz/fill_blank/comprehensive/mock',
    total_questions INT DEFAULT 0,
    answered_count INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    wrong_count INT DEFAULT 0,
    total_score DECIMAL(5,2),
    duration_seconds INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    config JSON COMMENT '扩展配置JSON',
    INDEX idx_practice_sessions_user (user_id),
    INDEX idx_practice_sessions_subject (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 错题本表
-- ========================================
CREATE TABLE IF NOT EXISTS wrong_question_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    subject_id BIGINT COMMENT '冗余，加速查询',
    wrong_count INT DEFAULT 1,
    consecutive_correct INT DEFAULT 0,
    last_wrong_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_reviewed_at TIMESTAMP NULL,
    next_review_at TIMESTAMP NULL,
    mastery_level INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_question (user_id, question_id),
    INDEX idx_wrong_book_user (user_id),
    INDEX idx_wrong_book_subject (user_id, subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 批改任务表
-- ========================================
CREATE TABLE IF NOT EXISTS grading_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    score INT,
    feedback TEXT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_grading_tasks_user (user_id),
    INDEX idx_grading_tasks_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 诊断报告表
-- ========================================
CREATE TABLE IF NOT EXISTS diagnostic_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id BIGINT COMMENT '关联会话',
    subject_id BIGINT COMMENT '诊断科目',
    report_type VARCHAR(20) DEFAULT 'session' COMMENT 'session/weekly/monthly',
    overall_score DECIMAL(5,1) COMMENT '综合得分',
    ability_rating VARCHAR(10) COMMENT '能力评级 S/A/B/C/D',
    radar_data JSON COMMENT '雷达图数据',
    weak_points JSON COMMENT '薄弱知识点TOP3',
    strengths JSON COMMENT '优势知识点',
    suggestions JSON COMMENT '改进建议',
    time_analysis JSON COMMENT '各题型平均用时',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_diagnostic_reports_user (user_id),
    INDEX idx_diagnostic_reports_subject (user_id, subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 成就表
-- ========================================
CREATE TABLE IF NOT EXISTS achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) COMMENT '成就编码',
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    icon_url VARCHAR(200) COMMENT '图标标识',
    category VARCHAR(50) COMMENT '成就类别',
    condition_type VARCHAR(50) COMMENT '解锁条件类型',
    condition_value INT COMMENT '条件阈值',
    sort_order INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 用户成就表
-- ========================================
CREATE TABLE IF NOT EXISTS user_achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    progress INT DEFAULT 0 COMMENT '进度 0-100',
    unlocked_at TIMESTAMP NULL COMMENT '解锁时间',
    notified_at TIMESTAMP NULL COMMENT '通知时间',
    UNIQUE KEY uk_user_achievement (user_id, achievement_id),
    INDEX idx_user_achievements_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 每日统计表
-- ========================================
CREATE TABLE IF NOT EXISTS daily_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    questions_answered INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    duration_sec INT DEFAULT 0,
    sessions_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    UNIQUE KEY uk_user_date (user_id, stat_date),
    INDEX idx_daily_stats_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
