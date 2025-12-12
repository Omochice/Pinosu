# Implementation Tasks — amber-login

以下はamber-login仕様の実装タスク一覧（優先度高→低）。各コミットは小さな意味単位にし、コミットメッセージはConventional Commitsを使用してください。仕様関連のコミットは `chore(spec):` プレフィックスを使用。

1. Add Amethyst Quartz dependency
   - 内容: build.gradle.kts に `quartz-android` 依存を追加
   - コミット: feat(auth): add amethyst quartz dependency

2. AmberSignerClient の実装（Data 層）
   - 内容: Intent生成、ActivityResultAPIのラッパー、Amber未インストール検出、レスポンス解析（pubkey取得）
   - コミット: feat(auth): implement AmberSignerClient

3. LocalAuthDataSource の実装（EncryptedSharedPreferences）
   - 内容: EncryptedSharedPreferences と Android Keystore を使った save/get/clear 実装
   - コミット: feat(storage): implement LocalAuthDataSource

4. AuthRepository の実装（Auth orchestration）
   - 内容: AmberSignerClient と LocalAuthDataSource を統合し loginWithAmber/logout/getLoginState を提供
   - コミット: feat(auth): implement AuthRepository

5. UseCase 層の実装（LoginUseCase, LogoutUseCase, GetLoginStateUseCase）
   - 内容: ビジネスロジックの実装とエラー分類
   - コミット: feat(auth): add login/logout usecases

6. LoginViewModel の実装（StateFlow を使用）
   - 内容: UIState 管理、UseCase 呼び出し、エラーハンドリング
   - コミット: feat(ui): implement LoginViewModel

7. UI 実装（Jetpack Compose）
   - 内容: LoginScreen と MainScreen の Composable、ローディング/ダイアログ表示、pubkey 表示
   - コミット: feat(ui): add login and main screens

8. AndroidManifest と queries 設定の追加
   - 内容: `<queries>` 要素、必要な intent-filter/permissions 設定を追加
   - コミット: chore(android): add manifest queries for Amber detection

9. Strings/Resources 日本語化とマスク関数の追加
   - 内容: strings.xml にエラーメッセージを追加、pubkey mask ユーティリティ実装
   - コミット: chore(i18n): add japanese strings for auth errors

10. DI 設定（Hilt / Koin）とテストセットアップ
    - 内容: AmberSignerClient, AuthRepository, UseCases, ViewModel の DI バインディングとテスト用モジュール
    - コミット: chore(di): add DI bindings for auth

11. テスト作成
    - 内容: Unit テスト（UseCase, Repository モック）, Integration テスト（ViewModel + UseCase）および Compose UI テストの骨組み
    - コミット: test(auth): add tests for auth flow

12. ドキュメント更新（仕様への実装進捗追記）
    - 内容: `.kiro/specs/amber-login` の `requirements.md`/`design.md` に実装状況を追記するコミットは `chore(spec):` を使用
    - コミット: chore(spec): update spec with implementation progress

実装順序の推奨: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12

---

次のステップ: どのタスクから引き継ぐか指示してください。継続承認がなければ、まず AmberSignerClient のスケルトン実装を開始します。
