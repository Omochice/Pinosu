# 研究・設計決定ログ

---
**目的**: 技術設計を支えるディスカバリー結果、アーキテクチャ調査、および根拠を記録する。

**使用法**:

- ディスカバリーフェーズでの調査活動と成果をログに記録
- `design.md`では詳細すぎる設計決定のトレードオフを文書化
- 将来の監査や再利用のための参照と証拠を提供

---

## サマリー

- **機能**: `amber-login`
- **ディスカバリースコープ**: 新機能 / 複雑な統合
- **主な発見**:
  - NIP-55がAmberとの統合の主要プロトコル（Intent-based通信）
  - Amethyst QuartzライブラリがNostrプロトコル実装を提供
  - Android Keystore + ActivityResultAPIが推奨されるモダンなセキュリティアプローチ

## 調査ログ

### NIP-46 (Nostr Connect) プロトコル

- **コンテキスト**: Amberログインの背後にある技術プロトコルの理解
- **参照元**:
  - [NIP-46 公式仕様](https://github.com/nostr-protocol/nips/blob/master/46.md)
  - [NIP-46 Documentation](https://nips.nostr.com/46)
- **発見**:
  - リモート署名者（bunker）とクライアント間の2-way通信フレームワーク
  - kind 24133イベントを使用し、NIP-44暗号化でJSON-RPC形式のメッセージを送信
  - 接続トークン形式: `bunker://<pubkey>?relay=<wss://relay>&secret=<secret>`
  - サポートメソッド: `connect`, `sign_event`, `get_public_key`, `nip44_encrypt/decrypt`
  - 秘密鍵はリモート署名者に隔離され、クライアントには公開されない
- **影響**:
  - AmberはNIP-46署名者として動作し、Pinosuはクライアントとして動作
  - ただし、Android環境ではNIP-55が実際の通信レイヤーとして機能
  - NIP-46の概念を理解することでアーキテクチャ設計が明確化

### NIP-55 (Android Signer Application) プロトコル

- **コンテキスト**: Android環境でのAmber統合の実装メカニズム
- **参照元**:
  - [NIP-55 公式仕様](https://github.com/nostr-protocol/nips/blob/master/55.md)
  - [NIP-55 Implementation Discussion](https://github.com/nostr-protocol/nips/pull/868)
- **発見**:
  - Android Intent-based通信とContent Resolverを使用
  - URIスキーム: `nostrsigner:<content>`
  - Amberパッケージ名: `com.greenart7c3.nostrsigner`
  - 主要操作:
    - `get_public_key`: ユーザーの公開鍵取得
    - `sign_event`: Nostrイベントへの署名
    - `nip04_encrypt/decrypt`: 旧暗号化方式
    - `nip44_encrypt/decrypt`: 新暗号化方式
  - 2つの通信方式:
    1. **Intent-based** (対話型): ユーザー確認が必要な操作
    2. **Content Resolver** (サイレント): 事前承認済み操作の自動実行
  - レスポンス処理: `result`, `event`, `rejected`カラム
  - パーミッションモデル: ユーザーが永続的に承認可能
- **影響**:
  - Intent-basedアプローチを採用（初回ログインには対話型が必須）
  - Android Manifest設定が必要（`<queries>`要素）
  - ActivityResultAPI使用が推奨（startActivityForResultは非推奨）
  - Amber未インストール時のハンドリングが必要

### Amber統合詳細

- **コンテキスト**: Amberアプリとの具体的な統合方法
- **参照元**:
  - [Amber GitHub Repository](https://github.com/greenart7c3/Amber)
  - [Amber on F-Droid](https://f-droid.org/en/packages/com.greenart7c3.nostrsigner/)
- **発見**:
  - Amberはスマートフォンをサーバーレス・追加ハードウェア不要のNIP-46署名デバイスとして機能
  - 複数アカウント対応、きめ細かいアプリ認可機能
  - 統合ドキュメントはソースコードから推測必要（公式ドキュメントは限定的）
  - 互換性: Amethyst, 0xChat, Voyage, Fountain, Pokeyなど
- **影響**:
  - NIP-55仕様に基づいて統合実装を行う
  - Amber未インストール時はGoogle Play Storeへの誘導が必要
  - エラーハンドリング: Intent解決失敗、レスポンスタイムアウト、ユーザー拒否

### Amethyst Quartzライブラリ

- **コンテキスト**: Nostrプロトコル実装の再利用
- **参照元**:
  - [Amethyst GitHub Repository](https://github.com/vitorpamplona/amethyst)
- **発見**:
  - Quartzは再利用可能なNostr-commonsライブラリ
  - Maven Centralで提供: `quartz-android`, `quartz-jvm`, `quartz-iosarm64`等
  - 主要クラス:
    - `KeyPair`: ユーザー認証情報管理
    - `NostrClient`: リレー接続管理
    - `Filter`: Nostrプロトコルフィルター構築
    - `NostrSignerExternal`: 外部署名者（Amber）統合
  - NIP-46サポートは不完全だが、`NostrSignerExternal`でAmber統合が可能
  - パラメータ: 公開鍵、パッケージ名、ContentResolver
- **影響**:
  - Quartzライブラリを依存関係として採用
  - `NostrSignerExternal`をAmber統合の基盤として使用
  - リレー通信が将来の機能拡張で必要となる場合に備えた設計

### Androidセキュリティベストプラクティス

- **コンテキスト**: 秘密鍵・セッションデータの安全な管理
- **参照元**:
  - [Android Keystore System](https://developer.android.com/privacy-and-security/keystore)
  - [Android Security Best Practices 2024](https://medium.com/@hiren6997/5-modern-android-security-practices-you-cant-ignore-in-2025-6560558be99e)
  - [Storing Secret Keys in Android](https://guides.codepath.com/android/Storing-Secret-Keys-in-Android)
- **発見**:
  - Android Keystoreは暗号鍵をTEE/SEに保存
  - 鍵マテリアルは抽出不可能（ハードウェアバック）
  - アクセス制御: 生体認証要求、時間的有効性、用途制限
  - 2024年時点で機密データを扱うアプリの43.7%のみがトラステッドハードウェア使用
  - EncryptedSharedPreferencesによるデータ暗号化
- **影響**:
  - ログイン状態（pubkey）はEncryptedSharedPreferencesで保存
  - セッションキーはAndroid Keystoreで保護
  - 秘密鍵はAmber側で管理されPinosuには保存しない
  - 生体認証は将来の機能拡張として考慮

### Jetpack Compose認証フロー

- **コンテキスト**: モダンなAndroid UIフレームワークでの認証実装
- **参照元**:
  - [Jetpack Compose Authentication Tutorial](https://auth0.com/blog/android-authentication-jetpack-compose-part-1/)
  - [Building Authentication Form with Jetpack Compose](https://joebirch.co/android/building-an-authentication-form-using-jetpack-compose/)
  - [Get a result from an activity](https://developer.android.com/training/basics/intents/result)
- **発見**:
  - MVVM architectureが推奨アプローチ
  - ComposeはStatelessなUIを推奨、StateはViewModelで管理
  - ActivityResultAPI (`registerForActivityResult`)がモダンな実装
  - `startActivityForResult()`は非推奨（deprecated）
  - IME actionsでフィールド間ナビゲーションを改善
  - Firebase AuthenticationやAuth0との統合パターンが豊富
- **影響**:
  - MVVM + Jetpack Composeアーキテクチャ採用
  - `registerForActivityResult`でAmber Intentレスポンス処理
  - 状態管理はViewModelで実施、ComposeはUI描画のみ
  - Clean Architectureパターンを適用し保守性を向上

### Android Intent通信（2024年更新）

- **コンテキスト**: アプリ間通信のモダンな実装方法
- **参照元**:
  - [Android Intents and Intent Filters](https://developer.android.com/guide/components/intents-filters)
  - [Get a result from an activity](https://developer.android.com/training/basics/intents/result)
- **発見**:
  - `startActivityForResult()`は非推奨
  - 新しいAPI: `ActivityResultLauncher` + `registerForActivityResult()`
  - Contract: `StartActivityForResult`が汎用的なIntent契約
  - Android 13+: 外部アプリからのIntentはexportedコンポーネントのみ
  - Implicit intent受信には`CATEGORY_DEFAULT`が必須
  - Multiple Intent防止: `FLAG_ACTIVITY_SINGLE_TOP`と`FLAG_ACTIVITY_CLEAR_TOP`使用
- **影響**:
  - ActivityResultAPIの採用
  - Manifest設定: exportedコンポーネント定義
  - Intent filter設定でAmber応答を適切に処理

## アーキテクチャパターン評価

| オプション | 説明 | 強み | リスク/制限 | 備考 |
|--------|------|------|----------|------|
| Clean Architecture + MVVM | レイヤー分離（Presentation, Domain, Data）とMVVMパターンの組み合わせ | テスタビリティ、保守性、関心の分離、Android推奨 | 初期実装の複雑性、小規模アプリには過剰 | Jetpack Composeとの親和性が高い |
| MVC (Model-View-Controller) | 従来型のAndroidパターン | シンプル、学習コスト低 | 状態管理が煩雑、Composeとの相性悪 | 非推奨（レガシーアプローチ） |
| Repository Pattern | データソースの抽象化 | データ層の切り替え容易、テスト容易 | 単純なアプリには過剰 | Clean Architectureの一部として採用 |

## 設計決定

### 決定: NIP-55を主要統合プロトコルとして採用

- **コンテキスト**: Amberとの通信方法の選定
- **検討した代替案**:
  1. NIP-46直接実装 — リレー経由の通信、サーバーレス
  2. NIP-55 (Intent-based) — Android標準のIntent機構
  3. カスタムIPC — 独自プロトコル実装
- **選定アプローチ**: NIP-55 (Intent-based)
- **根拠**:
  - Amberが公式にNIP-55をサポート
  - Android標準のIntent機構を使用するため信頼性が高い
  - ユーザー承認フローが組み込まれている
  - 他のNostr Androidアプリでも広く採用されている
- **トレードオフ**:
  - **利点**: Amber公式サポート、Android標準、セキュリティ、ユーザー体験
  - **妥協**: Android限定（iOSでは別実装必要）、Amberインストール必須
- **フォローアップ**: Amber未インストール時のフォールバック実装を確認

### 決定: Amethyst Quartzライブラリの採用

- **コンテキスト**: Nostrプロトコル実装の再利用 vs 独自実装
- **検討した代替案**:
  1. Amethyst Quartz — 実績あり、Nostr-commons
  2. nostr-java-library — 別のJVM実装
  3. 独自実装 — フルコントロール、依存関係なし
- **選定アプローチ**: Amethyst Quartz (`quartz-android`)
- **根拠**:
  - `NostrSignerExternal`がAmber統合を直接サポート
  - Amethystで実戦投入済み（信頼性）
  - 将来的なリレー通信やNIP実装の拡張が容易
  - Maven Centralで提供され依存関係管理が簡単
- **トレードオフ**:
  - **利点**: 実装時間短縮、実績ある実装、将来拡張性
  - **妥協**: 外部依存関係増加、ライブラリの学習コスト
- **フォローアップ**: バージョン互換性とライセンス確認

### 決定: Clean Architecture + MVVM + Jetpack Composeの採用

- **コンテキスト**: アプリケーションアーキテクチャの選定
- **検討した代替案**:
  1. Clean Architecture + MVVM — Android推奨、高保守性
  2. MVC — シンプル、レガシー
  3. MVP — Presenter層、古いパターン
- **選定アプローチ**: Clean Architecture + MVVM + Jetpack Compose
- **根拠**:
  - Google公式推奨アーキテクチャ
  - Jetpack Composeとの親和性が最も高い
  - テスタビリティ（ViewModel単体テスト可能）
  - 関心の分離により将来の機能拡張が容易
  - ログイン状態管理がViewModelで明確化
- **トレードオフ**:
  - **利点**: 保守性、テスタビリティ、拡張性、Android標準
  - **妥協**: 初期実装の学習コスト、ボイラープレートコード増加
- **フォローアップ**: レイヤー間の境界定義を明確化

### 決定: Android Keystore + EncryptedSharedPreferencesのセキュリティ戦略

- **コンテキスト**: ログイン状態とセッションデータの保存方法
- **検討した代替案**:
  1. EncryptedSharedPreferences — Android Jetpack Security
  2. SharedPreferences（平文） — セキュリティリスク高
  3. Room + SQLCipher — 過剰、将来のデータ拡張時に検討
- **選定アプローチ**: EncryptedSharedPreferences + Android Keystore
- **根拠**:
  - Android Jetpack Securityの標準実装
  - データはAES256で自動暗号化
  - マスターキーはAndroid Keystoreで保護（TEE/SE）
  - 実装が簡潔で信頼性が高い
  - 秘密鍵はAmber側で管理されるためPinosuは公開鍵のみ保存
- **トレードオフ**:
  - **利点**: セキュリティ、簡潔性、Android標準
  - **妥協**: Android 6.0+ (API 23+) 必須、鍵マテリアルのバックアップ不可
- **フォローアップ**: デバイス移行時のログイン再認証フロー

### 決定: ActivityResultAPIの採用

- **コンテキスト**: Amber Intentレスポンスの受信方法
- **検討した代替案**:
  1. ActivityResultAPI (`registerForActivityResult`) — モダン、推奨
  2. `startActivityForResult()` — 非推奨
- **選定アプローチ**: ActivityResultAPI
- **根拠**:
  - `startActivityForResult()`は非推奨（deprecated）
  - Android 13+での動作保証
  - Jetpack Composeとの統合が容易
  - Contract-based APIでタイプセーフ
  - ライフサイクル管理が自動化
- **トレードオフ**:
  - **利点**: モダンAPI、タイプセーフ、ライフサイクル管理
  - **妥協**: 新しいAPIの学習コスト
- **フォローアップ**: なし

## リスクと軽減策

- **リスク1: Amberアプリ未インストール**
  - 軽減策: Intent解決前に署名者アプリの存在確認、未インストール時はGoogle Play Storeへのディープリンク提供
- **リスク2: NIP-55レスポンスタイムアウト**
  - 軽減策: タイムアウト処理実装、ユーザーへの再試行オプション提供
- **リスク3: Amethyst Quartzライブラリのバージョン互換性**
  - 軽減策: 特定バージョンに固定、定期的なアップデート確認、deprecation警告への対応
- **リスク4: Android 13+のIntent制限**
  - 軽減策: Manifest設定で`<queries>`要素とexportedコンポーネントを適切に定義
- **リスク5: ユーザー認証拒否（rejected）**
  - 軽減策: ユーザーフィードバック提供、再試行ガイダンス、ログイン画面に留まる
- **リスク6: ログイン状態の永続化失敗**
  - 軽減策: EncryptedSharedPreferences書き込みエラーハンドリング、フォールバック実装

## 参照

- [NIP-46 Official Specification](https://github.com/nostr-protocol/nips/blob/master/46.md) — Nostr Connect remote signing protocol
- [NIP-55 Official Specification](https://github.com/nostr-protocol/nips/blob/master/55.md) — Android Signer Application specification
- [Amber GitHub Repository](https://github.com/greenart7c3/Amber) — Official Amber signer app
- [Amethyst GitHub Repository](https://github.com/vitorpamplona/amethyst) — Nostr client with Quartz library
- [Android Keystore System](https://developer.android.com/privacy-and-security/keystore) — Official Android security documentation
- [Android Intents and Intent Filters](https://developer.android.com/guide/components/intents-filters) — Android developer guide
- [Jetpack Compose Authentication Tutorial](https://auth0.com/blog/android-authentication-jetpack-compose-part-1/) — Modern Android authentication patterns
- [Get a result from an activity](https://developer.android.com/training/basics/intents/result) — ActivityResultAPI documentation
