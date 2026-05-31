package com.jaryjay.defender

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.security.MessageDigest
import kotlin.random.Random

object PinManager {
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_PIN_SALT = "pin_salt"
    private const val KEY_LAST_AUTH = "last_auth"
    private const val AUTH_WINDOW_MS = 30_000L

    fun isPinSet(context: Context): Boolean {
        return DefenderPreferences.prefs(context).getString(KEY_PIN_HASH, null) != null
    }

    fun setPin(context: Context, pin: String) {
        val salt = Random.nextBytes(16)
        val saltHex = salt.toHex()
        val hash = hashPin(pin, saltHex)
        DefenderPreferences.prefs(context).edit()
            .putString(KEY_PIN_SALT, saltHex)
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val prefs = DefenderPreferences.prefs(context)
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val expected = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return hashPin(pin, salt) == expected
    }

    fun isAuthorized(context: Context): Boolean {
        val lastAuth = DefenderPreferences.prefs(context).getLong(KEY_LAST_AUTH, 0L)
        return System.currentTimeMillis() - lastAuth <= AUTH_WINDOW_MS
    }

    fun recordAuthorized(context: Context) {
        DefenderPreferences.prefs(context).edit().putLong(KEY_LAST_AUTH, System.currentTimeMillis()).apply()
    }

    fun ensureAuthorized(activity: Activity): Boolean {
        if (isAuthorized(activity)) return true
        activity.startActivity(Intent(activity, PinEntryActivity::class.java))
        Toast.makeText(activity, "Unlock with PIN to edit", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun hashPin(pin: String, saltHex: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((saltHex + pin).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
