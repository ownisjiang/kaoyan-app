-- 研题库初始化脚本
-- 首次启动 MySQL 容器时自动执行，创建数据库和用户
CREATE DATABASE IF NOT EXISTS yantiku CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'yantiku'@'%' IDENTIFIED BY 'yantiku_pass_2026';
GRANT ALL PRIVILEGES ON yantiku.* TO 'yantiku'@'%';
FLUSH PRIVILEGES;
