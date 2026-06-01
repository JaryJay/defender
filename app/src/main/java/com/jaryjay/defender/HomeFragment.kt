package com.jaryjay.defender

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var pauseManager: PauseManager
    private lateinit var statsTracker: StatsTracker
    private lateinit var pauseStatus: TextView
    private lateinit var pauseValue: TextView
    private lateinit var pauseSlider: Slider
    private lateinit var timeSavedValue: TextView
    private lateinit var topAppsContainer: LinearLayout
    private lateinit var topSitesContainer: LinearLayout
    private lateinit var topAppsEmpty: TextView
    private lateinit var topSitesEmpty: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pauseManager = PauseManager(requireContext())
        statsTracker = StatsTracker(requireContext())

        pauseStatus = view.findViewById(R.id.pause_status)
        pauseValue = view.findViewById(R.id.pause_value)
        pauseSlider = view.findViewById(R.id.pause_slider)
        timeSavedValue = view.findViewById(R.id.time_saved_value)
        topAppsContainer = view.findViewById(R.id.top_apps_container)
        topSitesContainer = view.findViewById(R.id.top_sites_container)
        topAppsEmpty = view.findViewById(R.id.top_apps_empty)
        topSitesEmpty = view.findViewById(R.id.top_sites_empty)

        pauseSlider.value = DEFAULT_PAUSE_MINUTES.toFloat()
        updatePauseValue(DEFAULT_PAUSE_MINUTES)
        pauseSlider.addOnChangeListener { _, value, _ ->
            updatePauseValue(value.toInt())
        }

        view.findViewById<Button>(R.id.pause_button).setOnClickListener {
            if (!PinManager.ensureAuthorized(requireActivity())) return@setOnClickListener
            val minutes = pauseSlider.value.toInt()
            if (!pauseManager.pauseFor(minutes)) {
                Toast.makeText(requireContext(), getString(R.string.pause_error), Toast.LENGTH_SHORT).show()
            }
            updatePauseStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePauseStatus()
        renderStats()
    }

    private fun updatePauseValue(minutes: Int) {
        pauseValue.text = getString(R.string.pause_slider_value, minutes)
    }

    private fun updatePauseStatus() {
        val status = when {
            pauseManager.isPaused() -> getString(R.string.pause_status_paused, pauseManager.remainingMinutes().toInt())
            pauseManager.canPause() -> getString(R.string.pause_status_available)
            else -> getString(R.string.pause_status_used)
        }
        pauseStatus.text = status
    }

    private fun renderStats() {
        val summary = statsTracker.getCurrentSummary()
        val minutesSaved = (summary.appBlocks + summary.siteBlocks) * MINUTES_SAVED_PER_BLOCK
        timeSavedValue.text = getString(R.string.time_saved_format, minutesSaved)

        val topApps = statsTracker.getTopAppBlocks(TOP_LIST_LIMIT)
        renderAppStats(topApps)

        val topSites = statsTracker.getTopSiteBlocks(TOP_LIST_LIMIT)
        renderSiteStats(topSites)
    }

    private fun renderAppStats(entries: List<StatsTracker.CountEntry>) {
        topAppsContainer.removeAllViews()
        if (entries.isEmpty()) {
            topAppsEmpty.visibility = View.VISIBLE
            return
        }
        topAppsEmpty.visibility = View.GONE
        val inflater = LayoutInflater.from(requireContext())
        entries.forEach { entry ->
            val row = inflater.inflate(R.layout.list_item_block_app, topAppsContainer, false)
            val labelInfo = AppInfoUtils.resolveAppInfo(requireContext(), entry.key)
            row.findViewById<TextView>(R.id.item_text).text =
                getString(R.string.blocked_count_format, labelInfo.label, entry.count)
            row.findViewById<View>(R.id.remove_button).visibility = View.GONE
            row.findViewById<android.widget.ImageView>(R.id.item_icon).setImageDrawable(labelInfo.icon)
            topAppsContainer.addView(row)
        }
    }

    private fun renderSiteStats(entries: List<StatsTracker.CountEntry>) {
        topSitesContainer.removeAllViews()
        if (entries.isEmpty()) {
            topSitesEmpty.visibility = View.VISIBLE
            return
        }
        topSitesEmpty.visibility = View.GONE
        val inflater = LayoutInflater.from(requireContext())
        entries.forEach { entry ->
            val row = inflater.inflate(R.layout.list_item_block, topSitesContainer, false)
            row.findViewById<TextView>(R.id.item_text).text =
                getString(R.string.blocked_count_format, entry.key, entry.count)
            row.findViewById<View>(R.id.remove_button).visibility = View.GONE
            topSitesContainer.addView(row)
        }
    }

    companion object {
        private const val DEFAULT_PAUSE_MINUTES = 15
        private const val TOP_LIST_LIMIT = 3
        private const val MINUTES_SAVED_PER_BLOCK = 1
    }
}
