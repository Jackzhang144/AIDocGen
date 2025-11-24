CREATE TABLE IF NOT EXISTS docs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NULL,
    user_id VARCHAR(64),
    email VARCHAR(255),
    output MEDIUMTEXT,
    prompt MEDIUMTEXT,
    language VARCHAR(64),
    time_to_generate BIGINT,
    time_to_call BIGINT,
    source VARCHAR(64),
    feedback_id VARCHAR(64),
    feedback INT,
    is_preview TINYINT(1),
    has_accepted_preview TINYINT(1),
    is_explained TINYINT(1),
    doc_format VARCHAR(32),
    comment_format VARCHAR(32),
    kind VARCHAR(64),
    is_selection TINYINT(1),
    prompt_id VARCHAR(64),
    actual_language VARCHAR(64),
    model_provider VARCHAR(64),
    latency_ms BIGINT
);

CREATE TABLE IF NOT EXISTS api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hashed_key VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    purpose VARCHAR(255),
    created_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS ai_provider_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(64) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    base_url VARCHAR(255),
    model VARCHAR(128),
    temperature DOUBLE DEFAULT 0.2,
    max_output_tokens INT DEFAULT 512,
    enabled TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(32) NOT NULL,
    api_quota INT DEFAULT 100,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doc_jobs (
    job_id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    payload LONGTEXT NOT NULL,
    state VARCHAR(32) NOT NULL,
    reason TEXT,
    result LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_doc_jobs_user FOREIGN KEY (user_id) REFERENCES users(id)
);
