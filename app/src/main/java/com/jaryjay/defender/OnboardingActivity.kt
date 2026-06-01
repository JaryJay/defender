package com.jaryjay.defender

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    private lateinit var flipper: ViewFlipper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        flipper = findViewById(R.id.onboarding_flipper)
        findViewById<Button>(R.id.onboarding_next).setOnClickListener {
            flipper.showNext()
        }
        findViewById<Button>(R.id.onboarding_accessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.onboarding_done).setOnClickListener {
            if (AccessibilityUtils.isAccessibilityEnabled(this)) {
                startMain()
            } else {
                Toast.makeText(this, getString(R.string.enable_accessibility), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (AccessibilityUtils.isAccessibilityEnabled(this)) {
            startMain()
        }
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
