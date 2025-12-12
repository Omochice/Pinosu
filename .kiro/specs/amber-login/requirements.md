# Requirements Document

## Project Description (Input)

nostrのkind10003のbookmark機能を扱うアプリを作りたい

ログインはnip46をつかってamberでログインするようにしたい。

nostrのロジックを扱うのはamethystにライブラリがあるらしいのでそれを使いたい。

まず、amberでログインするだけのアプリを作ってみて。

commitは最小意味単位でconventional commitに従ってcommitして。

仕様関連の各ステップは `chore(spec):` のプレフィックスでコミットすること。

## Introduction

本仕様書は、Nostr kind 10003のブックマーク機能を提供するAndroidアプリケーション「Pinosu」の初期フェーズとして、Amber経由でのNIP-46認証機能を定義する。本フェーズでは、Amberアプリケーションを使用したNostrアカウントへのログイン機能のみを実装対象とする。

## Requirements

### Requirement 1: Amber連携による認証

**Objective:** As a Nostrユーザー, I want Amberアプリを使ってログインできる機能, so that セキュアにNostrアカウントにアクセスできる

#### Acceptance Criteria

1. When ユーザーがログインボタンをタップ, the Pinosuアプリ shall Amberアプリへのインテント連携を開始する
2. When Amberアプリが端末にインストールされていない, the Pinosuアプリ shall Amberのインストールを促すダイアログを表示する
3. When Amberからの認証レスポンスを受信, the Pinosuアプリ shall NIP-46接続を確立する
4. When 認証が成功, the Pinosuアプリ shall ユーザーの公開鍵（pubkey）を取得して保存する
5. If Amberからの認証がキャンセルまたは失敗, then the Pinosuアプリ shall エラーメッセージを表示してログイン画面に留まる

### Requirement 2: ログイン状態の管理

**Objective:** As a Nostrユーザー, I want ログイン状態が永続化される機能, so that アプリを再起動してもログイン状態を維持できる

#### Acceptance Criteria

1. When 認証が成功, the Pinosuアプリ shall ログイン状態をローカルストレージに保存する
2. When アプリを起動, the Pinosuアプリ shall 保存されたログイン状態を確認する
3. While ログイン済み状態, the Pinosuアプリ shall ログイン画面をスキップしてメイン画面を表示する
4. The Pinosuアプリ shall ログアウト機能を提供する
5. When ログアウトボタンをタップ, the Pinosuアプリ shall 保存されたログイン状態をクリアする

### Requirement 3: ユーザーインターフェース

**Objective:** As a Nostrユーザー, I want シンプルで分かりやすいログイン画面, so that 迷わずにログインできる

#### Acceptance Criteria

1. The Pinosuアプリ shall ログイン画面に「Amberでログイン」ボタンを配置する
2. While ログイン処理中, the Pinosuアプリ shall ローディングインジケーターを表示する
3. When ログイン成功, the Pinosuアプリ shall ログイン成功を示すメッセージを表示する
4. The Pinosuアプリ shall メイン画面にログアウトボタンを配置する
5. The Pinosuアプリ shall メイン画面にログイン中のpubkeyを表示する

### Requirement 4: NIP-46プロトコルの実装

**Objective:** As a 開発者, I want NIP-46仕様に準拠した実装, so that 標準的なNostr認証を実現できる

#### Acceptance Criteria

1. The Pinosuアプリ shall NIP-46のNostr Connectプロトコルを実装する
2. When Amberとの通信, the Pinosuアプリ shall NIP-46で定義されたメッセージフォーマットを使用する
3. The Pinosuアプリ shall Amethystライブラリを活用してNostrプロトコルを実装する
4. The Pinosuアプリ shall 認証に必要な暗号化・復号化処理を実装する
5. When 通信エラーが発生, the Pinosuアプリ shall 適切なエラーハンドリングを実行する

### Requirement 5: エラーハンドリングとユーザーフィードバック

**Objective:** As a Nostrユーザー, I want エラー時に適切なフィードバック, so that 問題を理解して対処できる

#### Acceptance Criteria

1. If Amberアプリが見つからない, then the Pinosuアプリ shall Google Play Storeへのリンクとエラーメッセージを表示する
2. If ネットワークエラーが発生, then the Pinosuアプリ shall ネットワーク接続を確認するよう促すメッセージを表示する
3. If Amberからのレスポンスが不正, then the Pinosuアプリ shall エラー詳細をログに記録して汎用エラーメッセージを表示する
4. If タイムアウトが発生, then the Pinosuアプリ shall タイムアウトを通知して再試行オプションを提供する
5. The Pinosuアプリ shall すべてのエラーメッセージを日本語で表示する

### Requirement 6: セキュリティとプライバシー

**Objective:** As a Nostrユーザー, I want 秘密鍵が安全に管理される機能, so that アカウントのセキュリティが保たれる

#### Acceptance Criteria

1. The Pinosuアプリ shall 秘密鍵を一切保存しない（Amber側で管理）
2. The Pinosuアプリ shall ローカルストレージに保存するデータを暗号化する
3. When センシティブなデータをログ出力, the Pinosuアプリ shall マスキング処理を適用する
4. The Pinosuアプリ shall 通信時にHTTPSを使用する（該当する場合）
5. The Pinosuアプリ shall Android Keystoreを活用してセッションキーを保護する
