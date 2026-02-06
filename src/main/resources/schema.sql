drop table if exists review;
drop table if exists chat;
drop table if exists favorite_item;
drop table if exists app_order;
drop table if exists item;
drop table if exists category;
drop table if exists users;


create table users (
	id bigserial primary key,
	name varchar(255) not null,  -- nullable = false
	email varchar(255) unique,  -- unique = true
	password varchar(255) not null,  -- nullable = false
	role varchar(255) not null,  -- nullable = false
	line_notify_token varchar(255),  -- LINE Notify トークン
	enabled boolean not null default true,  -- enabled フラグ
	banned BOOLEAN NOT NULL DEFAULT FALSE,
    ban_reason VARCHAR(255)
);

create table category (
	id bigserial primary key,
	name varchar(255) not null unique  -- nullable = false, unique = true
);

create table item (
	id bigserial primary key,
	-- 出品者(User)への外部キー
	user_id bigint not null references users(id), -- seller 
	name varchar(255) not null,
	-- description: columnDefinition = "TEXT"
	description text,
	-- 価格: BigDecimalはNUMERICが適切
	price numeric not null,
	-- カテゴリ (Category) への外部キー (NULL 許容)
	category_id bigint references category(id),
	status varchar(255), -- status (デフォルト値は Java 側で設定)
	image_url varchar(255),
	-- 作成日時
	created_at timestamp without time zone not null
);

create table app_order (
	id bigserial primary key,
	-- 商品 (Item) への外部キー
	item_id bigint not null references item(id),
	-- 買い手 (User) への外部キー
	buyer_id bigint not null references users(id),
	price numeric not null,
	status varchar(255) not null,
	-- paymentIntentId: unique = true
	payment_intent_id varchar(255) unique,
	created_at timestamp without time zone not null
);

create table chat (
	id bigserial primary key,
	-- 対象商品 (Item) への外部キー
	item_id bigint not null references item(id),
	-- 送信者 (User) への外部キー
	sender_id bigint not null references users(id),
	-- message: columnDefinition = "TEXT"
	message text,
	created_at timestamp without time zone not null
);

create table favorite_item (
	id bigserial primary key,
	-- ユーザー (User) への外部キー
	user_id bigint not null references users(id),
	-- 商品 (Item) への外部キー
	item_id bigint not null references item(id),
	created_at timestamp without time zone not null
	-- NOTE: user_id と item_id の複合ユニーク制約を Entity に追加しても良いでしょう
);

create table review (
	id bigserial primary key,
	-- 注文 (AppOrder) への外部キー。unique = true で 1 対 1 を担保
	order_id bigint not null unique references app_order(id), -- OneToOne
	-- レビューワ (User) への外部キー
	reviewer_id bigint not null references users(id),
	-- 被評価者 (User) への外部キー
	seller_id bigint not null references users(id),
	-- 対象商品 (Item) への外部キー
	item_id bigint not null references item(id),
	rating integer not null,
	-- comment: columnDefinition = "TEXT"
	comment text,
	created_at timestamp without time zone not null
);
