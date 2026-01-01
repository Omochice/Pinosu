package io.github.omochice.pinosu.domain.model

import org.junit.Assert.*import org.junit.test


/** Normal pubkey (Bech32 format)User test that User can be created */ @test
 fun `create User with valid pubkey`() {
 val validPubkey = "npub1" + "a".repeat(59)
 val user = User(validPubkey)

 assertEquals(validPubkey, user.pubkey)
 }

/** Different pubkey (Bech32 format, numbers lowercase)User test that User can be created */ @test
 fun `create User with valid hex pubkey containing numbers`() {
 val validPubkey = "npub1" + "0123456789abcdef".repeat(3) + "0123456789a"
 val user = User(validPubkey)

 assertTrue(user.pubkey.startsWith("npub1"))
 assertEquals(validPubkey, user.pubkey)
 }

/** npub1does not start with pubkeyIllegalArgumentExceptionis thrown test */ @test(expected = IllegalArgumentException::class)
 fun `create User with pubkey too short throws exception`() {
val shortPubkey = "npub" + "a".repeat(59) // npub1 npubstarts with User(shortPubkey)
 }

/** 16format (format)ofpubkeyIllegalArgumentExceptionis thrown test */ @test(expected = IllegalArgumentException::class)
 fun `create User with pubkey too long throws exception`() {
val longPubkey = "a".repeat(64) // npub1does not start with16format User(longPubkey)
 }

/** npub1does not start with pubkey (NPUB1)IllegalArgumentExceptionis thrown test */ @test(expected = IllegalArgumentException::class)
 fun `create User with uppercase characters throws exception`() {
val invalidPubkey = "NPUB1" + "A".repeat(59) // ofNPUB1 User(invalidPubkey)
 }

/** nsecstarts withpubkey (format)IllegalArgumentExceptionis thrown test */ @test(expected = IllegalArgumentException::class)
 fun `create User with non-hex characters throws exception`() {
val invalidPubkey = "nsec1" + "g".repeat(59) // nsec1offormat User(invalidPubkey)
 }

/** ofpubkeyIllegalArgumentExceptionis thrown test */ @test(expected = IllegalArgumentException::class)
 fun `create User with empty pubkey throws exception`() {
 User("")
 }

/** nprofile1starts withpubkey (format)IllegalArgumentExceptionis thrown test */ @test(expected = IllegalArgumentException::class)
 fun `create User with spaces throws exception`() {
val invalidPubkey = "nprofile1" + "a".repeat(54) // nprofile1format User(invalidPubkey)
 }

/** Userofequality (data classof)test */ @test
 fun `User equality works correctly`() {
 val pubkey = "npub1" + "a".repeat(59)
 val user1 = User(pubkey)
 val user2 = User(pubkey)

 assertEquals(user1, user2)
 assertEquals(user1.hashCode(), user2.hashCode())
 }

/** differentpubkeyUser not test */ @test
 fun `Users with different pubkeys are not equal`() {
 val user1 = User("npub1" + "a".repeat(59))
 val user2 = User("npub1" + "b".repeat(59))

 assertNotEquals(user1, user2)
 }
}
