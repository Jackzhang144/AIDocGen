CREATE DATABASE IF NOT EXISTS doc_generator;
USE doc_generator;

-- 用户表
CREATE TABLE IF NOT EXISTS users
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_uid           VARCHAR(255) UNIQUE,
    email              VARCHAR(255) NOT NULL UNIQUE,
    name               VARCHAR(255),
    given_name         VARCHAR(255),
    family_name        VARCHAR(255),
    picture            VARCHAR(1024),
    password           VARCHAR(255),
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_active_at     DATETIME,
    last_login_at      DATETIME,
    refresh_token      TEXT,
    plan               VARCHAR(50),
    stripe_customer_id VARCHAR(255),
    updated_at         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 文档表
CREATE TABLE IF NOT EXISTS docs
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              VARCHAR(255),
    email                VARCHAR(255),
    output               TEXT,
    prompt               TEXT NOT NULL,
    language             VARCHAR(50),
    time_to_generate     INT,
    time_to_call         INT,
    source               VARCHAR(100),
    feedback_id          VARCHAR(255),
    feedback             INT,
    is_preview           BOOLEAN  DEFAULT FALSE,
    has_accepted_preview BOOLEAN  DEFAULT FALSE,
    is_explained         BOOLEAN  DEFAULT FALSE,
    doc_format           VARCHAR(50),
    comment_format       VARCHAR(50),
    kind                 VARCHAR(50),
    is_selection         BOOLEAN  DEFAULT TRUE,
    prompt_id            VARCHAR(255),
    actual_language      VARCHAR(50),
    timestamp            DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_docs_user_id (user_id)
);

-- API密钥表
CREATE TABLE IF NOT EXISTS api_keys
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    hashed_key VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    email      VARCHAR(255),
    purpose    VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 团队表
CREATE TABLE IF NOT EXISTS teams
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin   VARCHAR(255) NOT NULL,
    members JSON NOT NULL DEFAULT (JSON_ARRAY()),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
