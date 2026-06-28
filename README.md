# 研题库 (YanTiKu)

考研冲刺刷题平台 — 选择题 / 填空题 / 综合题 / 模考，AI 批改，错题诊断，诊断报告

---

## 功能特性

| 模块 | 功能 |
|------|------|
| 题库 | 6 个科目，按方向/科目/知识点三级组织，支持筛选和搜索 |
| 选择题 | 10 题一组，点击选项自动跳下一题，提交后即时判分 |
| 填空题 | 内嵌输入框 + 数学符号栏，AI 批改评分 |
| 综合题 | 长文本作答，AI 根据要点覆盖度综合评分 |
| 模考 | 20 题混合模式（10 选择 + 7 填空 + 3 综合），真实考试体验 |
| 错题本 | 自动收录错题，支持复习和掌握度追踪 |
| 诊断报告 | 雷达图 + 薄弱点分析 + 时间分析 |
| 成就系统 | 累计答题、正确率、连续打卡等成就徽章 |
| 个人中心 | 学习时长 / 连续天数 / 每日统计 |

---

## 技术栈

| 层 | 技术 |
|---|------|
| 后端框架 | Spring Boot 3.3 + Spring Cloud Gateway 2023.0.3 |
| 注册中心 | Nacos 2.3（服务发现 + 配置中心） |
| 数据库 | MySQL 8.0 + Flyway 自动迁移 |
| 缓存 | Redis 7 |
| 消息队列 | RabbitMQ 3.12 |
| ORM | MyBatis + MyBatis-Plus |
| AI 批改 | DeepSeek API（Chat 模型） |
| 前端 | Vue 3 + Vite + Vue Router |
| 部署 | Docker Compose（8 容器） |

---

## 微服务架构

```
                         ┌─────────────────┐
                         │  MySQL   :3306   │
                         └────────┬────────┘
                                  │
  Client ──► Gateway :8080 ─── Auth      :8081  (认证/用户/统计/迁移)
                     │         Question  :8082  (题库/科目/考点)
                     │         Practice  :8083  (刷题/AI批改/错题/诊断/成就)
                     │
                     └─────── Redis :6379  (缓存)
                              RabbitMQ :5672 (消息队列)
                              Nacos :8848    (注册中心)
```

---

## 快速开始

```bash
# 1. 环境变量（可选）
cp .env.example .env
# 编辑 .env，填入 LLM_API_KEY 等密钥

# 2. 启动
docker compose up -d
```

访问：`http://localhost`

---

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `JWT_ACCESS_SECRET` | - | JWT 签名密钥（生产必须修改） |
| `JWT_REFRESH_SECRET` | - | Refresh Token 密钥 |
| `MYSQL_ROOT_PASS` | `yantiku_root_2026` | MySQL root 密码 |
| `MYSQL_USER` / `MYSQL_PASS` | `yantiku` / `yantiku_pass_2026` | 应用数据库账号 |
| `RABBITMQ_USER` / `RABBITMQ_PASS` | `yantiku` / `yantiku_pass_2026` | 消息队列账号 |
| `LLM_API_KEY` | （空） | DeepSeek API Key（不填则回退文本匹配） |

> 详细配置见 `.env.example`

---

## 模块说明

| 模块 | 说明 |
|------|------|
| `yantiku-common` | 公共模块：`ApiResponse`、异常类、`@CurrentUser` 注解 |
| `yantiku-gateway` | API 网关：路由分发 + JWT 验证 + `X-User-Id` 透传 |
| `yantiku-auth` | 认证服务：手机号登录/注册、JWT 签发刷新、用户管理、统计、Flyway 数据库迁移 |
| `yantiku-question` | 题库服务：方向/科目/知识点/题目 CRUD、分页筛选、随机出题 |
| `yantiku-practice` | 刷题服务：会话管理、批量提交、AI 批改、错题本、诊断报告、成就系统 |

---

## 项目结构

```
kaoyan-app/
├── docker-compose.yml          # 8 容器编排
├── schema.sql                  # 完整表结构
├── .env.example                # 环境变量模板
├── README.md
│
├── docker/
│   ├── Dockerfile.backend      # 后端通用 Dockerfile（4 微服务共用，ARG 区分）
│   ├── Dockerfile.frontend     # 前端 Dockerfile
│   ├── mysql/conf/ + init/     # MySQL 配置
│   ├── redis/conf/             # Redis 配置
│   ├── rabbitmq/               # RabbitMQ 配置
│   └── nginx/                  # Nginx 配置
│
├── backend/
│   ├── pom.xml                 # 父 POM
│   ├── yantiku-common/         # 公共模块（DTO/异常/工具/注解）
│   ├── yantiku-gateway/        # API 网关（路由 + JWT 验证）
│   ├── yantiku-auth/           # 认证服务（登录/注册/用户/统计/Flyway 迁移）
│   ├── yantiku-question/       # 题库服务（科目/知识点/题目 CRUD）
│   └── yantiku-practice/       # 刷题服务（会话/批改/错题/诊断/成就）
│
└── frontend/
    ├── index.html
    ├── vite.config.js
    └── src/
        ├── views/              # Home | Quiz | FillBlank | Comprehensive | MockExam | Report | WrongBook | Profile
        ├── api/                # API 层
        ├── components/         # SideNav / TopBar
        └── router/             # Vue Router
```

---

## API 路由

| 路径 | 服务 | 说明 |
|------|------|------|
| `/api/v1/auth/**` | Auth | 登录/注册/刷新Token（公开） |
| `/api/v1/users/**` | Auth | 用户信息/统计 |
| `/api/v1/directions/**` | Question | 考试方向 |
| `/api/v1/subjects/**` | Question | 科目列表/详情 |
| `/api/v1/knowledge-points/**` | Question | 知识点 |
| `/api/v1/questions/**` | Question | 题目查询 |
| `/api/v1/practice/**` | Practice | 刷题会话/提交/掌握度 |
| `/api/v1/diagnostic/**` | Practice | 诊断报告 |
| `/api/v1/achievements/**` | Practice | 成就系统 |
| `/api/v1/wrong-book/**` | Practice | 错题本 |
