package com.jaryjay.defender

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private var ignoreSwitchChanges = false
    private lateinit var hapticSwitch: SwitchCompat
    private lateinit var summarySwitch: SwitchCompat
    private lateinit var summaryTimeLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.settings_toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        hapticSwitch = findViewById(R.id.haptic_switch)
        summarySwitch = findViewById(R.id.summary_switch)
        summaryTimeLabel = findViewById(R.id.summary_time_label)

        hapticSwitch.isChecked = DefenderPreferences.isHapticEnabled(this)
        hapticSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (ignoreSwitchChanges) return@setOnCheckedChangeListener
            if (!PinManager.ensureAuthorized(this)) {
                revertSwitch(hapticSwitch)
                return@setOnCheckedChangeListener
            }
            DefenderPreferences.setHapticEnabled(this, isChecked)
        }

        summarySwitch.isChecked = DefenderPreferences.isSummaryEnabled(this)
        summarySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (ignoreSwitchChanges) return@setOnCheckedChangeListener
            if (!PinManager.ensureAuthorized(this)) {
                revertSwitch(summarySwitch)
                return@setOnCheckedChangeListener
            }
            DefenderPreferences.setSummaryEnabled(this, isChecked)
            if (isChecked) {
                requestNotificationsIfNeeded()
                SummaryScheduler.scheduleNext(this)
            } else {
                SummaryScheduler.cancel(this)
            }
        }

        findViewById<Button>(R.id.summary_time_button).setOnClickListener {
            if (!PinManager.ensureAuthorized(this)) return@setOnClickListener
            showTimePicker()
        }

        findViewById<Button>(R.id.change_pin).setOnClickListener {
            if (!PinManager.ensureAuthorized(this)) return@setOnClickListener
            startActivity(Intent(this, PinEntryActivity::class.java).putExtra(PinEntryActivity.EXTRA_RESET_PIN, true))
        }

        updateSummaryTimeLabel()
    }

    override fun onResume() {
        super.onResume()
        updateSummaryTimeLabel()
        ignoreSwitchChanges = true
        hapticSwitch.isChecked = DefenderPreferences.isHapticEnabled(this)
        summarySwitch.isChecked = DefenderPreferences.isSummaryEnabled(this)
        ignoreSwitchChanges = false
    }

    private fun updateSummaryTimeLabel() {
        val minutes = DefenderPreferences.getSummaryMinutes(this)
        val hour = minutes / 60
        val minute = minutes % 60
        summaryTimeLabel.text = String.format(
            Locale.getDefault(),
            "%s: %02d:%02d",
            getString(R.string.summary_time),
            hour,
            minute
        )
    }

    private fun showTimePicker() {
        val minutes = DefenderPreferences.getSummaryMinutes(this)
        val initialHour = minutes / 60
        val initialMinute = minutes % 60
        val dialog = TimePickerDialog(this, { _, hour, minute ->
            DefenderPreferences.setSummaryMinutes(this, hour * 60 + minute)
            SummaryScheduler.scheduleNext(this)
            updateSummaryTimeLabel()
        }, initialHour, initialMinute, true)
        dialog.show()
    }

    private fun revertSwitch(switchCompat: SwitchCompat) {
        ignoreSwitchChanges = true
        switchCompat.isChecked = !switchCompat.isChecked
        ignoreSwitchChanges = false
    }

    private fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2001)
            }
        }
    }
}
