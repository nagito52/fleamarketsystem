-- 初期管理者ユーザー（固定）
INSERT INTO users (name, email, password, role, enabled, banned, ban_reason)
VALUES ('運営者', 'admin@example.com', 'adminpass', 'ROLE_ADMIN', TRUE, FALSE, NULL)
ON CONFLICT (email) DO UPDATE
SET name = EXCLUDED.name,
    password = EXCLUDED.password,
    role = EXCLUDED.role,
    enabled = EXCLUDED.enabled,
    banned = FALSE,
    ban_reason = NULL;

-- 初期カテゴリ
INSERT INTO category (name) VALUES
 ('本'), ('家電'), ('ファッション'), ('おもちゃ'), ('文房具')
ON CONFLICT (name) DO NOTHING;