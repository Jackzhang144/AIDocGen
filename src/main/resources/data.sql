INSERT INTO users (email, name, password, plan, created_at, updated_at) VALUES
('admin@example.com', 'Admin User', 'password', 'premium', NOW(), NOW()),
('user1@example.com', 'Test User 1', 'password', 'free', NOW(), NOW()),
('user2@example.com', 'Test User 2', 'password', 'free', NOW(), NOW());

INSERT INTO api_keys (hashed_key, email, purpose) VALUES
('key_1234567890', 'admin@example.com', 'testing'),
('key_0987654321', 'user1@example.com', 'development');

INSERT INTO teams (admin, members) VALUES
('admin@example.com', '["user1@example.com", "user2@example.com"]'),
('user1@example.com', '["user2@example.com"]');