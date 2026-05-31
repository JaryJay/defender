package com.jaryjay.defender

import java.net.URI

object WildcardMatcher {
    fun matchesAny(patterns: List<String>, host: String): Boolean {
        return patterns.any { matches(it, host) }
    }

    fun matches(pattern: String, host: String): Boolean {
        val normalizedPattern = normalizePattern(pattern)
        if (normalizedPattern.isBlank()) return false
        val normalizedHost = normalizeHost(host) ?: return false
        if (matchesHost(normalizedPattern, normalizedHost)) return true
        val withoutWww = normalizedHost.removePrefix("www.")
        return withoutWww != normalizedHost && matchesHost(normalizedPattern, withoutWww)
    }

    fun normalizeHost(input: String): String? {
        val trimmed = input.trim().lowercase()
        if (trimmed.isBlank()) return null
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        val host = runCatching { URI(withScheme).host }.getOrNull() ?: return null
        return host.trimEnd('.').lowercase()
    }

    private fun normalizePattern(pattern: String): String {
        val trimmed = pattern.trim().lowercase()
        val withoutScheme = trimmed.removePrefix("http://").removePrefix("https://")
        return withoutScheme.substringBefore("/")
    }

    private fun matchesHost(pattern: String, host: String): Boolean {
        val regex = buildRegex(pattern)
        return regex.matches(host)
    }

    private fun buildRegex(pattern: String): Regex {
        val builder = StringBuilder()
        pattern.forEach { ch ->
            when (ch) {
                '*' -> builder.append(".*")
                '.' -> builder.append("\\.")
                else -> builder.append(Regex.escape(ch.toString()))
            }
        }
        return Regex("^$builder$", RegexOption.IGNORE_CASE)
    }
}
