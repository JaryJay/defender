package com.jaryjay.defender

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WildcardMatcherTest {
    @Test
    fun matchesExactDomain() {
        assertTrue(WildcardMatcher.matches("instagram.com", "instagram.com"))
        assertFalse(WildcardMatcher.matches("instagram.com", "mail.instagram.com"))
    }

    @Test
    fun matchesWildcardDomain() {
        assertTrue(WildcardMatcher.matches("*.instagram.com", "www.instagram.com"))
        assertTrue(WildcardMatcher.matches("*.instagram.com", "mail.instagram.com"))
        assertFalse(WildcardMatcher.matches("*.instagram.com", "instagram.com"))
    }

    @Test
    fun matchesWithSchemeAndPath() {
        assertTrue(WildcardMatcher.matches("https://news.ycombinator.com", "news.ycombinator.com"))
        assertTrue(WildcardMatcher.matches("*.example.com", "https://foo.example.com/path"))
    }

    @Test
    fun normalizesHost() {
        assertTrue(WildcardMatcher.matches("example.com", "www.example.com"))
    }
}
