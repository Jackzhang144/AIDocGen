-- 清理旧数据，确保脚本可重复执行
DELETE FROM docs;
DELETE FROM teams;
DELETE FROM api_keys;
DELETE FROM users;

-- 示例用户（包含 user_uid 便于与 doc 记录关联）
INSERT INTO users (user_uid, email, name, plan, created_at, last_active_at, updated_at)
VALUES ('admin-uid-001', 'admin@example.com', 'Admin User', 'premium', NOW(), NOW(), NOW()),
       ('user-uid-002', 'user1@example.com', 'Test User 1', 'free', NOW(), NOW(), NOW()),
       ('user-uid-003', 'user2@example.com', 'Test User 2', 'free', NOW(), NOW(), NOW());

-- 示例 API Key（值为 SHA-1 哈希，括号内备注原始明文）
INSERT INTO api_keys (hashed_key, email, purpose)
VALUES ('70597006238a5b9d1bc328e197a3575fd7059d89', 'admin@example.com', 'admin testing key (plain: admin-api-key-456)'),
       ('2e2b11b144a5bd9e95a40cf9bc48dc5583a5bb00', 'user1@example.com', 'demo key for SDK (plain: demo-api-key-123)');

-- 示例团队（最多 3 人），便于验证邀请/撤销逻辑
INSERT INTO teams (admin, members, created_at)
VALUES ('admin@example.com', JSON_ARRAY('user1@example.com', 'user2@example.com'), NOW()),
       ('user1@example.com', JSON_ARRAY('user2@example.com'), NOW());

-- 示例生成结果，用于测试队列/反馈/统计逻辑
INSERT INTO docs (user_id, email, output, prompt, language, time_to_generate, time_to_call, source, feedback_id,
                  feedback, is_preview, has_accepted_preview, is_explained, doc_format, comment_format, kind,
                  is_selection, prompt_id, actual_language, timestamp)
VALUES ('user-uid-002', 'user1@example.com', '/** Adds two numbers */\nfunction add(a, b) {\n  return a + b;\n}',
        'write:v3', 'javascript', 3200, 900, 'vscode', 'fb-1111-aaaa', 1, FALSE, FALSE, TRUE,
        'Auto', 'JSDoc', 'function', TRUE, 'prompt-1111-aaaa', 'javascript', NOW()),
       ('admin-uid-001', 'admin@example.com', '"""Calculate the factorial of a number."""\n\ndef factorial(n):\n    if n <= 1:\n        return 1\n    return n * factorial(n - 1)',
        'write:v3', 'python', 4100, 1200, 'intellij', 'fb-2222-bbbb', NULL, FALSE, FALSE, TRUE,
        'Google', 'Docstring', 'function', FALSE, 'prompt-2222-bbbb', 'python', NOW()),
       ('user-uid-003', 'user2@example.com', '/*\n * Checks whether a string is a palindrome.\n */\nbool isPalindrome(std::string_view input) {\n    auto left = input.begin();\n    auto right = input.end();\n    if (left == right) {\n        return true;\n    }\n    --right;\n    while (left < right) {\n        if (*left != *right) {\n            return false;\n        }\n        ++left;\n        --right;\n    }\n    return true;\n}\n',
        'write:v3', 'cpp', 2800, 850, 'web', 'fb-3333-cccc', -1, TRUE, FALSE, TRUE,
        'DocBlock', 'DocBlock', 'function', TRUE, 'prompt-3333-cccc', 'cpp', NOW());
