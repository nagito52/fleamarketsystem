-- users テーブルの既存データをクリア（もし ddl-auto=create でない場合のため）
-- DELETE FROM users; 

-- 初期ユーザー登録
INSERT INTO users (name, email, password, role, enabled) VALUES
 ('出品者 A', 'sellerA@example.com', 'password', 'ROLE_USER', TRUE),
 ('購入者 B', 'xyz@example.com', 'password', 'ROLE_USER', TRUE),
 ('運営者 C', 'adminC@example.com', 'adminpass', 'ROLE_ADMIN', TRUE);

-- category
INSERT INTO category (name) VALUES
 ('本'), ('家電'), ('ファッション'), ('おもちゃ'), ('文房具');

-- item（出品者AのIDを動的に取得して紐付け）
INSERT INTO item (user_id, name, description, price, category_id, status, created_at) VALUES
 (
   (SELECT id FROM users WHERE email='sellerA@example.com'), 
   'Java プログラミング入門', '初心者向けの Java 入門書です。', 1500.00, 
   (SELECT id FROM category WHERE name='本'), '出品中', CURRENT_TIMESTAMP
 ),
 (
   (SELECT id FROM users WHERE email='sellerA@example.com'), 
   'ワイヤレスイヤホン', 'ノイズキャンセリング機能付き。', 8000.00, 
   (SELECT id FROM category WHERE name='家電'), '出品中', CURRENT_TIMESTAMP
 );