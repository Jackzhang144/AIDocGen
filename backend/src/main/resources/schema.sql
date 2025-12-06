CREATE TABLE IF NOT EXISTS ai_request_logs (
                                               id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                               request_type VARCHAR(50) DEFAULT NULL COMMENT '请求类型: comment, explain, document',
    file_name VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    username VARCHAR(255) DEFAULT NULL COMMENT '用户名',
    prompt_snippet TEXT COMMENT '发送给AI的代码片段(前500字符)',
    created_at DATETIME(6) DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI请求日志表';

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';
