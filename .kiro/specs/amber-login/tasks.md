# 実装計画

## タスク一覧

### 1. プロジェクトセットアップと依存関係の設定

- [x] 1.1 (P) Androidプロジェクトの初期化とGradle設定
    - Android SDK 26 (minSdk)、targetSdk 34の設定
    - Kotlin 1.9+の設定とCoroutinesサポート有効化
    - ビルド設定でJetpack Composeを有効化
    - _Requirements: 4.3_

- [x] 1.2 (P) 必要な依存関係の追加
    - Jetpack Compose BOM（最新安定版）とUI依存関係
    - Android ViewModel (Lifecycle 2.6+) とlifecycle-runtime-compose
    - Amethyst Quartz (com.vitorpamplona:quartz-android) の追加
    - Android Jetpack Security (androidx.security:security-crypto 1.1+)
    - Hilt/Koin（依存性注入ライブラリ）の追加
    - JUnit 5、Mockk、Compose Testing Libraryの追加
    - _Requirements: 4.3, 6.2, 6.5_

- [x] 1.3 (P) Androidマニフェストの基本設定
    - アプリケーション名とアイコンの設定
    - `<queries>` 要素の追加（Amber Intent検出用）
    - `android:scheme="nostrsigner"` のIntent Filter設定
    - Exported Activity設定（Android 13+対応）
    - _Requirements: 1.2, 4.1, 4.2_

### 2. データモデルとエラー型定義

- [x] 2.1 (P) ドメインモデルの実装
    - Userエンティティの作成（pubkey: String）
    - Pubkey検証ロジック（64文字16進数形式）の実装
    - 将来拡張用のAuthEvent sealed classの定義
    - _Requirements: 1.4, 6.1_

- [x] 2.2 (P) エラー型の定義
    - LoginError sealed class（AmberNotInstalled, UserRejected, Timeout, NetworkError, UnknownError）
    - LogoutError sealed class（StorageError）
    - StorageError sealed class（WriteError, ReadError）
    - AmberError sealed class（NotInstalled, UserRejected, Timeout, InvalidResponse, IntentResolutionError）
    - _Requirements: 1.5, 4.5, 5.1, 5.2, 5.3, 5.4_

### 3. ローカルストレージ実装（Data層）

- [x] 3.1 LocalAuthDataSourceの実装
    - EncryptedSharedPreferencesの初期化処理
    - Android Keystore経由のMasterKey生成（AES256_GCM）
    - AES256-SIVキー暗号化、AES256-GCM値暗号化の設定
    - _Requirements: 6.2, 6.5_

- [x] 3.2 ユーザーデータの保存・取得・削除機能
    - saveUser(user: User): Result<Unit, StorageError> の実装
    - getUser(): User? の実装（nullチェック、検証ロジック）
    - clearLoginState(): Result<Unit, StorageError> の実装
    - created_atとlast_accessed タイムスタンプの管理
    - _Requirements: 1.4, 2.1, 2.2, 2.5_

- [x] 3.3 LocalAuthDataSourceの単体テスト
    - 保存・取得・削除の正常系テスト
    - 不正データ読み込み時のエラーハンドリングテスト
    - 暗号化・復号化の正常動作確認（モック使用）
    - _Requirements: 2.1, 2.2, 2.5, 6.2_

### 4. Amber通信クライアント実装（Data層）

- [x] 4.1 AmberSignerClientの基本実装
    - checkAmberInstalled(): Boolean の実装（PackageManager使用）
    - Amber未インストール検出ロジック
    - AmberResponse、AmberError データクラスの定義
    - _Requirements: 1.2, 5.1_

- [x] 4.2 NIP-55 Intent構築とリクエスト送信
    - createPublicKeyIntent(): Intent の実装
    - Intent構築（nostrsigner: スキーム、type: get_public_key）
    - パッケージ名明示（com.greenart7c3.nostrsigner）
    - FLAG_ACTIVITY_SINGLE_TOPとFLAG_ACTIVITY_CLEAR_TOPの設定
    - ActivityResultLauncherとの統合は Task 10.3 で実装予定
    - _Requirements: 1.1, 1.3, 4.1, 4.2_

- [x] 4.3 Amberレスポンス処理とタイムアウト管理
    - handleAmberResponse(resultCode: Int, data: Intent?): Result<AmberResponse> の実装
    - Intent Extrasからの結果抽出（result, rejected）
    - ユーザー拒否検出（rejected=true、RESULT_CANCELED）
    - Pubkey形式検証（64文字16進数）
    - 不正レスポンスのエラーハンドリング
    - タイムアウト処理は Repository層で実装予定（withTimeout使用）
    - _Requirements: 1.3, 1.5, 4.5, 5.3, 5.4_

- [x] 4.4 センシティブデータのログマスキング
    - pubkeyマスキング関数の実装（最初8文字+...+最後8文字）
    - maskPubkey()関数を実装、16文字以下はマスキングなし
    - 実際のログ出力への適用はRepository/UseCase層で実装予定
    - _Requirements: 6.3_

- [x]* 4.5 AmberSignerClientの単体テスト
    - Amber未インストール検出テスト (3 tests) ✓
    - Intent構築の正確性テスト (5 tests) ✓
    - レスポンス解析テスト（正常系・異常系） (7 tests) ✓
    - タイムアウト処理テストはRepository層で実装予定
    - 全27テストがTDD手法で tasks 4.1-4.4 実装時に完了済み
    - _Requirements: 1.2, 1.3, 1.5, 4.1, 4.2, 4.5_

### 5. リポジトリ実装（Data層）

- [x] 5.1 AuthRepositoryの実装
    - processAmberResponse(): AmberレスポンスのJSON parsing and validation ✓
    - getLoginState(): ローカルストレージからユーザー取得 ✓
    - saveLoginState(user): ユーザーのローカル保存 ✓
    - logout(): ログイン状態のクリア ✓
    - checkAmberInstalled(): Amber installed verification ✓
    - 10テスト実装済み (全テストパス)
    - ActivityResultAPI統合は task 10.3 で実装予定
    - _Requirements: 1.3, 1.4, 2.1, 2.2, 2.4, 2.5_

- [x] 5.2 エラーハンドリングとトランザクション管理
    - Amber通信失敗時のエラー分類（AmberNotInstalled, UserRejected, Timeout, NetworkError）✓
    - トランザクション的整合性の保証（Amber成功 → ローカル保存失敗時の対応）✓
    - 14テスト実装済み (全テストパス)
    - Note: ローカル保存失敗時のリトライは設計上「不可」(design.md L920)
    - _Requirements: 1.5, 4.5, 5.2, 5.3, 6.1_

- [x]* 5.3 AuthRepositoryの単体テスト
    - Amber成功 → ローカル保存成功の正常系テスト ✓
    - Amber失敗時のエラー分類テスト ✓
    - ログアウト処理テスト ✓
    - トランザクション整合性テスト ✓
    - 14テスト実装済み (tasks 5.1, 5.2で完了)
    - _Requirements: 1.3, 1.4, 1.5, 2.1, 2.5_

### 6. ドメイン層UseCasesの実装

- [ ] 6.1 (P) LoginUseCaseの実装
    - suspend operator fun invoke(): Result<User, LoginError> の実装
    - AuthRepository.loginWithAmber() の呼び出し
    - エラーハンドリングとエラー分類
    - _Requirements: 1.1, 1.3, 1.4, 1.5, 4.5_

- [ ] 6.2 (P) LogoutUseCaseの実装
    - suspend operator fun invoke(): Result<Unit, LogoutError> の実装
    - AuthRepository.logout() の呼び出し
    - 冪等性の保証
    - _Requirements: 2.4, 2.5_

- [ ] 6.3 (P) GetLoginStateUseCaseの実装
    - suspend operator fun invoke(): User? の実装
    - AuthRepository.getLoginState() の呼び出し
    - 読み取り専用操作の保証
    - _Requirements: 2.2, 2.3_

- [ ]* 6.4 UseCasesの単体テスト
    - LoginUseCase正常系・異常系テスト
    - LogoutUseCase正常系・異常系テスト
    - GetLoginStateUseCase正常系・異常系テスト
    - _Requirements: 1.1, 1.3, 1.4, 1.5, 2.2, 2.3, 2.4, 2.5_

### 7. ViewModelとUI状態管理（Presentation層）

- [ ] 7.1 LoginViewModelの実装
    - LoginUiState、MainUiState データクラスの定義
    - StateFlow<LoginUiState>、StateFlow<MainUiState> の実装
    - onLoginButtonClicked()、onLogoutButtonClicked() の実装
    - onRetryLogin()、dismissError()、checkLoginState() の実装
    - _Requirements: 1.1, 1.5, 2.2, 2.3, 2.4, 3.2, 3.3, 3.5_

- [ ] 7.2 ViewModelScopeでのCoroutine実行とエラーハンドリング
    - UseCases呼び出しの非同期処理
    - エラー時のStateFlow更新ロジック
    - ローディング状態管理（isLoading フラグ）
    - ログイン成功時のナビゲーション制御
    - _Requirements: 1.5, 3.2, 3.3, 5.2, 5.4_

- [ ] 7.3 依存性注入の設定
    - Hilt/KoinによるViewModel注入設定
    - UseCasesの注入設定
    - _Requirements: 該当なし（技術実装）_

- [ ]* 7.4 LoginViewModelの単体テスト
    - ログインボタンタップ → UseCases呼び出し → UI状態更新のテスト
    - エラーハンドリングフロー（AmberNotInstalled → showAmberInstallDialog更新）のテスト
    - ログアウトフローのテスト
    - _Requirements: 1.1, 1.5, 2.2, 2.3, 2.4, 3.2, 3.3_

### 8. ログイン画面UI実装（Presentation層）

- [ ] 8.1 LoginScreenの基本実装
    - Jetpack Composable関数の作成
    - LoginViewModel.uiState の collectAsState() 観察
    - 「Amberでログイン」ボタンの配置
    - ローディングインジケーターの表示ロジック
    - _Requirements: 3.1, 3.2_

- [ ] 8.2 エラーダイアログの実装
    - Amber未インストールダイアログ（Play Storeリンク付き）
    - タイムアウトダイアログ（再試行ボタン付き）
    - 汎用エラーダイアログ
    - ユーザー拒否エラーダイアログ
    - _Requirements: 1.2, 1.5, 5.1, 5.4_

- [ ] 8.3 ログイン成功時のナビゲーション
    - ログイン成功メッセージの表示
    - メイン画面への画面遷移
    - _Requirements: 3.3_

- [ ]* 8.4 LoginScreenの単体テスト
    - ボタン表示テスト
    - ローディング状態表示テスト
    - エラーダイアログ表示テスト
    - _Requirements: 3.1, 3.2, 3.3_

### 9. メイン画面UI実装（Presentation層）

- [ ] 9.1 MainScreenの基本実装
    - Jetpack Composable関数の作成
    - LoginViewModel.mainUiState の collectAsState() 観察
    - ログアウトボタンの配置
    - ユーザーpubkeyの表示（部分マスキング推奨）
    - _Requirements: 2.3, 3.4, 3.5_

- [ ] 9.2 ログアウト処理とナビゲーション
    - ログアウトボタンタップハンドリング
    - ログイン画面への画面遷移
    - _Requirements: 2.4_

- [ ]* 9.3 MainScreenの単体テスト
    - pubkey表示テスト
    - ログアウトボタン表示テスト
    - ナビゲーションテスト
    - _Requirements: 2.3, 3.4, 3.5_

### 10. ナビゲーションとアプリ統合

- [ ] 10.1 アプリ起動時のログイン状態確認
    - MainActivity onCreate() での GetLoginStateUseCase 呼び出し
    - ログイン済み → メイン画面表示
    - 未ログイン → ログイン画面表示
    - 不正データ検出時のログイン状態クリア
    - _Requirements: 2.2, 2.3_

- [ ] 10.2 Navigation Composeの統合
    - NavHostの設定
    - ログイン画面とメイン画面のルート定義
    - 画面遷移ロジック（loginSuccess → navigateToMainScreen）
    - Back Press処理
    - _Requirements: 2.3, 3.3_

- [ ] 10.3 ActivityResultAPIの統合
    - registerForActivityResult の設定
    - AmberSignerClientへのActivityResultLauncher渡し
    - Amber Intent結果のハンドリング
    - _Requirements: 1.1, 1.3_

### 11. 日本語リソース文字列とローカライゼーション

- [ ] 11.1 (P) strings.xmlの作成
    - Amber関連エラーメッセージ（error_amber_not_installed, error_user_rejected, error_timeout, error_invalid_response）
    - システムエラーメッセージ（error_storage_write, error_storage_read, error_generic）
    - ビジネスロジックエラーメッセージ（error_invalid_pubkey）
    - UI文字列（ログインボタン、ログアウトボタン、ログイン成功メッセージ）
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

### 12. 統合テスト実装

- [ ]* 12.1 Presentation層とDomain層の統合テスト
    - LoginViewModel + UseCases統合テスト
    - エラーハンドリングフロー統合テスト
    - ログアウトフロー統合テスト
    - _Requirements: 1.1, 1.5, 2.4_

- [ ]* 12.2 Data層の統合テスト
    - AuthRepository + AmberSignerClient + LocalAuthDataSource 統合テスト
    - EncryptedSharedPreferences実動作テスト（保存 → 取得 → 削除）
    - _Requirements: 1.3, 1.4, 2.1, 2.5_

### 13. UIテスト実装（End-to-End）

- [ ]* 13.1 主要ユーザーフローのUIテスト
    - ログインフロー（ログイン画面 → ログインボタンタップ → ローディング表示 → メイン画面遷移）
    - Amber未インストールエラーフロー
    - ログアウトフロー（メイン画面 → ログアウトボタンタップ → ログイン画面遷移）
    - _Requirements: 1.1, 1.2, 2.4, 3.1, 3.2, 3.3, 3.4_

- [ ]* 13.2 アプリ再起動とログイン状態復元のテスト
    - ログイン済み状態でアプリ起動 → メイン画面表示
    - 未ログイン状態でアプリ起動 → ログイン画面表示
    - _Requirements: 2.2, 2.3_

- [ ]* 13.3 エラーシナリオのUIテスト
    - ユーザー拒否エラー（ログイン画面 → ログインボタンタップ → Amber拒否 → エラーメッセージ表示）
    - タイムアウトエラー（再試行オプションの表示確認）
    - _Requirements: 1.5, 5.4_

## タスク実装順序の推奨

**フェーズ1: 基盤構築**

- 1.1, 1.2, 1.3 （プロジェクトセットアップ、依存関係、マニフェスト設定）
- 2.1, 2.2 （データモデルとエラー型定義）

**フェーズ2: Data層実装**

- 3.1, 3.2 （LocalAuthDataSource）
- 4.1, 4.2, 4.3, 4.4 （AmberSignerClient）
- 5.1, 5.2 （AuthRepository）

**フェーズ3: Domain層実装**

- 6.1, 6.2, 6.3 （UseCases）

**フェーズ4: Presentation層実装**

- 7.1, 7.2, 7.3 （LoginViewModel）
- 11.1 （日本語リソース文字列）
- 8.1, 8.2, 8.3 （LoginScreen）
- 9.1, 9.2 （MainScreen）

**フェーズ5: 統合と動作確認**

- 10.1, 10.2, 10.3 （ナビゲーション、ActivityResultAPI統合）

**フェーズ6: テスト実装（オプション、MVP後に実施可能）**

- 3.3, 4.5, 5.3, 6.4, 7.4, 8.4, 9.3 （単体テスト）
- 12.1, 12.2 （統合テスト）
- 13.1, 13.2, 13.3 （UIテスト）

## 要件カバレッジサマリー

**Requirement 1（Amber連携による認証）**:

- 1.1: 1.1, 1.3, 4.2, 6.1, 7.1, 8.1, 10.3, 12.1, 13.1
- 1.2: 1.3, 4.1, 8.2, 13.1
- 1.3: 1.3, 4.2, 4.3, 5.1, 6.1, 10.3, 12.2
- 1.4: 2.1, 3.2, 5.1, 6.1, 12.2
- 1.5: 2.2, 4.3, 5.2, 6.1, 7.1, 7.2, 8.2, 12.1, 13.3

**Requirement 2（ログイン状態の管理）**:

- 2.1: 3.2, 5.1, 12.2
- 2.2: 3.2, 6.3, 7.1, 10.1, 13.2
- 2.3: 6.3, 7.1, 9.1, 10.1, 10.2, 13.2
- 2.4: 5.1, 6.2, 7.1, 9.2, 12.1, 13.1
- 2.5: 3.2, 5.1, 5.2, 6.2, 12.2

**Requirement 3（ユーザーインターフェース）**:

- 3.1: 8.1, 13.1
- 3.2: 7.1, 7.2, 8.1, 13.1
- 3.3: 7.1, 7.2, 8.3, 10.2, 13.1
- 3.4: 9.1, 13.1
- 3.5: 7.1, 9.1

**Requirement 4（NIP-46プロトコルの実装）**:

- 4.1: 1.3, 4.2
- 4.2: 1.3, 4.2
- 4.3: 1.1, 1.2
- 4.4: （暗号化処理はQuartz/Amber側で実装されるため直接的な実装タスクなし）
- 4.5: 2.2, 4.3, 5.2, 6.1

**Requirement 5（エラーハンドリングとユーザーフィードバック）**:

- 5.1: 2.2, 4.1, 8.2, 11.1
- 5.2: 2.2, 5.2, 7.2, 11.1
- 5.3: 2.2, 4.3, 11.1
- 5.4: 2.2, 4.3, 7.2, 8.2, 11.1, 13.3
- 5.5: 11.1

**Requirement 6（セキュリティとプライバシー）**:

- 6.1: 2.1, 5.2
- 6.2: 1.2, 3.1
- 6.3: 4.4
- 6.4: （本フェーズでは該当なし、Intent通信のため）
- 6.5: 1.2, 3.1

## 並列実行可能タスク

**(P)マーカー付きタスク**は、他のタスクとの依存関係がなく、並列実行が可能です。以下のグループは同時に作業できます:

**グループ1（セットアップ）**:

- 1.1, 1.2, 1.3

**グループ2（モデル定義）**:

- 2.1, 2.2

**グループ3（UseCases）**:

- 6.1, 6.2, 6.3（前提: AuthRepositoryが完成）

**グループ4（リソース）**:

- 11.1（独立して作業可能）

## 注記

- `- [ ]*` でマークされたタスクは、MVP後に実施可能なオプションのテストタスクです
- テストタスクは機能実装後に実施することを推奨しますが、TDD（テスト駆動開発）アプローチを採用する場合は、実装前にテストを作成できます
- すべてのコミットは Conventional Commit 形式に従い、最小意味単位でコミットしてください
- 仕様関連の変更は `chore(spec):` プレフィックスを使用してください
