# Fairsty

就職活動で提示することを目的に開発した、Spring Boot 製フリーマーケットアプリです。  
「売る・買う・やり取りする」を 1 つのサービスで完結できるように設計しています。

## プロジェクト概要

- **アプリ名**: Fairsty
- **種別**: Webアプリケーション（サーバーサイドレンダリング）
- **主な利用者**: 一般ユーザー（出品者・購入者）、管理者
- **開発目的**: 個人開発で要件定義から実装まで一貫して取り組み、実務で求められる設計力・実装力を示す

## このプロジェクトで伝えたい強み（就活向け）

- **要件を機能に落とし込む力**: 取引フロー、権限制御、問い合わせ対応などを仕様化して実装
- **バックエンド中心の実装力**: Spring Security/JPA/トランザクション制御を用いた堅牢な設計
- **外部API連携力**: Stripe、Cloudinary、LINE Messaging API を統合
- **改善サイクル**: 実装後の不具合修正、運用を意識した設定、UI/UX改善を継続

## 主な機能

- ユーザー登録・ログイン・権限制御（一般ユーザー / 管理者）
- 商品の出品・編集・購入・お気に入り
- 画像アップロードとトリミング（Cropper.js）
- 取引ステータス管理（出品中 / 取引中 / 発送済 / 売却済）
- 購入者キャンセル制御（発送通知後はキャンセル不可）
- 管理者による取引監視と強制キャンセル（取引中のみ）
- お問い合わせ送信と管理画面での確認
- 外部通知・連携（LINE Messaging API、Stripe、Cloudinary）

## 技術スタック

- **Backend**: Java 17, Spring Boot, Spring Security, Spring Data JPA, Thymeleaf
- **Frontend**: HTML, CSS, JavaScript（Vanilla JS）, Cropper.js
- **Database**: PostgreSQL
- **Build**: Maven
- **External Services**: Stripe API, Cloudinary, LINE Messaging API

## ディレクトリ構成（抜粋）

```text
src/main/java/com/example/fleamarketsystem
  ├─ controller
  ├─ service
  ├─ repository
  ├─ entity
  └─ config

src/main/resources
  ├─ templates
  ├─ static
  │  ├─ css
  │  └─ js
  ├─ application.properties.example
  ├─ schema.sql
  └─ data.sql
```

## 要件定義について

要件定義は作成済みです。  
README では実装観点の要約を記載し、詳細な仕様・画面遷移・ユースケースは要件定義書で管理しています。

> 補足: 要件定義書をリポジトリに含める場合は `docs/requirements.md` などに配置し、ここからリンクしてください。

## セットアップ手順

### 1) 前提

- Java 17
- Maven
- PostgreSQL

### 2) 設定ファイル作成

`src/main/resources/application.properties.example` をコピーして `application.properties` を作成し、環境変数または値を設定してください。

主に設定する項目:

- DB接続（`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`）
- Stripe（`STRIPE_API_KEY`, `STRIPE_PUBLIC_KEY`）
- Cloudinary（`CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`）
- LINE（`LINE_MESSAGING_TOKEN`, `LINE_MESSAGING_USER_ID`）

### 3) 起動

```bash
./mvnw spring-boot:run
```

起動後、`http://localhost:8080` へアクセスしてください。

## 初期データ

- 起動時に `schema.sql` / `data.sql` を実行する設定です。
- 管理者ユーザーとカテゴリの初期データを投入します。

## 今後の改善予定

- テストコード拡充（単体・統合・E2E）
- セキュリティ強化（パスワード暗号化、CSRF運用整理）
- パフォーマンス改善（クエリ最適化、ページネーション）
- 運用改善（監視、CI/CD）

## 関連ドキュメント

- 開発振り返り: `DEVELOPMENT_REFLECTION.md`

