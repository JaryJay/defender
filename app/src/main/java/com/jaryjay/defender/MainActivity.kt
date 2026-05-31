package com.jaryjay.defender

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var repository: BlockListRepository
    private lateinit var pauseManager: PauseManager
    private var ignoreSwitchChanges = false

    private lateinit var alwaysAppsContainer: LinearLayout
    private lateinit var blockedAppsContainer: LinearLayout
    private lateinit var alwaysSitesContainer: LinearLayout
    private lateinit var blockedSitesContainer: LinearLayout
    private lateinit var pauseStatus: TextView
    private lateinit var pauseMinutes: EditText
    private lateinit var hapticSwitch: SwitchCompat
    private lateinit var summarySwitch: SwitchCompat
    private lateinit var summaryTimeLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = BlockListRepository(this)
        pauseManager = PauseManager(this)

        alwaysAppsContainer = findViewById(R.id.always_apps_container)
        blockedAppsContainer = findViewById(R.id.blocked_apps_container)
        alwaysSitesContainer = findViewById(R.id.always_sites_container)
        blockedSitesContainer = findViewById(R.id.blocked_sites_container)
        pauseStatus = findViewById(R.id.pause_status)
        pauseMinutes = findViewById(R.id.pause_minutes)
        hapticSwitch = findViewById(R.id.haptic_switch)
        summarySwitch = findViewById(R.id.summary_switch)
        summaryTimeLabel = findViewById(R.id.summary_time_label)

        findViewById<Button>(R.id.open_accessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        setupAddButton(R.id.add_always_app, R.id.input_always_app) { repository.addAlwaysBlockedApp(it) }
        setupAddButton(R.id.add_blocked_app, R.id.input_blocked_app) { repository.addBlockedApp(it) }
        setupAddButton(R.id.add_always_site, R.id.input_always_site) { repository.addAlwaysBlockedSite(it) }
        setupAddButton(R.id.add_blocked_site, R.id.input_blocked_site) { repository.addBlockedSite(it) }

        findViewById<Button>(R.id.pause_button).setOnClickListener {
            if (!PinManager.ensureAuthorized(this)) return@setOnClickListener
            val minutes = pauseMinutes.text.toString().toIntOrNull() ?: 0
            if (!pauseManager.pauseFor(minutes)) {
                Toast.makeText(this, "Pause already used today or invalid minutes", Toast.LENGTH_SHORT).show()
            }
            updatePauseStatus()
        }

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
        updatePauseStatus()
        refreshLists()
        SummaryScheduler.scheduleNext(this)
        if (summarySwitch.isChecked) {
            requestNotificationsIfNeeded()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshLists()
        updatePauseStatus()
        updateSummaryTimeLabel()
        ignoreSwitchChanges = true
        hapticSwitch.isChecked = DefenderPreferences.isHapticEnabled(this)
        summarySwitch.isChecked = DefenderPreferences.isSummaryEnabled(this)
        ignoreSwitchChanges = false
    }

    private fun setupAddButton(buttonId: Int, inputId: Int, addAction: (String) -> Unit) {
        val button = findViewById<Button>(buttonId)
        val input = findViewById<EditText>(inputId)
        button.setOnClickListener {
            if (!PinManager.ensureAuthorized(this)) return@setOnClickListener
            val value = input.text.toString().trim()
            if (value.isBlank()) return@setOnClickListener
            addAction(value)
            input.text.clear()
            refreshLists()
        }
    }

    private fun refreshLists() {
        renderList(alwaysAppsContainer, repository.getAlwaysBlockedApps()) {
            repository.removeAlwaysBlockedApp(it)
        }
        renderList(blockedAppsContainer, repository.getBlockedApps()) {
            repository.removeBlockedApp(it)
        }
        renderList(alwaysSitesContainer, repository.getAlwaysBlockedSites()) {
            repository.removeAlwaysBlockedSite(it)
        }
        renderList(blockedSitesContainer, repository.getBlockedSites()) {
            repository.removeBlockedSite(it)
        }
    }

    private fun renderList(container: LinearLayout, items: List<String>, onRemove: (String) -> Unit) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)
        items.forEach { item ->
            val row = inflater.inflate(R.layout.list_item_block, container, false)
            val text = row.findViewById<TextView>(R.id.item_text)
            val remove = row.findViewById<View>(R.id.remove_button)
            text.text = item
            remove.setOnClickListener {
                if (!PinManager.ensureAuthorized(this)) return@setOnClickListener
                onRemove(item)
                refreshLists()
            }
            container.addView(row)
        }
    }

    private fun updatePauseStatus() {
        val status = when {
            pauseManager.isPaused() -> "Paused for ${pauseManager.remainingMinutes()} min remaining"
            pauseManager.canPause() -> "Pause available"
            else -> "Pause used today"
        }
        pauseStatus.text = status
    }

    private fun updateSummaryTimeLabel() {
        val minutes = DefenderPreferences.getSummaryMinutes(this)
        val hour = minutes / 60
        val minute = minutes % 60
        summaryTimeLabel.text = String.format(Locale.getDefault(), "%s: %02d:%02d", getString(R.string.summary_time), hour, minute)
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
