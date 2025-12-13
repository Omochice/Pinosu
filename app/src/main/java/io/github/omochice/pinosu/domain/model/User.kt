package io.github.omochice.pinosu.domain.model

/**
 * Userエンティティ（集約ルート）
 *
 * Nostrユーザーのログイン状態を表現するドメインモデル。 公開鍵（pubkey）のみを保持し、秘密鍵は一切保存しない。
 *
 * Task 2.1: ドメインモデルの実装 Requirements: 1.4, 6.1
 *
 * @property pubkey Nostr公開鍵（64文字の16進数形式）
 * @throws IllegalArgumentException pubkeyが不正な形式の場合
 */
data class User(val pubkey: String) {
  init {
    require(pubkey.isValidNostrPubkey()) {
      "Invalid Nostr pubkey format: must be 64 hex characters"
    }
  }
}

/**
 * Nostr公開鍵の検証用拡張関数
 *
 * 公開鍵が64文字の16進数（0-9, a-f）であることを検証する。
 *
 * @return 有効な公開鍵形式の場合true、それ以外はfalse
 */
private fun String.isValidNostrPubkey(): Boolean {
  return this.matches(Regex("^[0-9a-f]{64}$"))
}
