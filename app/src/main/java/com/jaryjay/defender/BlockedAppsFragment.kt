package com.jaryjay.defender

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class BlockedAppsFragment : Fragment(R.layout.fragment_blocked_apps) {
    private lateinit var repository: BlockListRepository
    private lateinit var alwaysAppsContainer: LinearLayout
    private lateinit var blockedAppsContainer: LinearLayout
    private lateinit var alwaysAppsEmpty: TextView
    private lateinit var blockedAppsEmpty: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = BlockListRepository(requireContext())

        alwaysAppsContainer = view.findViewById(R.id.always_apps_container)
        blockedAppsContainer = view.findViewById(R.id.blocked_apps_container)
        alwaysAppsEmpty = view.findViewById(R.id.always_apps_empty)
        blockedAppsEmpty = view.findViewById(R.id.blocked_apps_empty)

        view.findViewById<Button>(R.id.add_always_app).setOnClickListener {
            if (!PinManager.ensureAuthorized(requireActivity())) return@setOnClickListener
            startActivity(
                AppPickerActivity.newIntent(requireContext(), AppPickerActivity.BLOCK_TYPE_ALWAYS)
            )
        }
        view.findViewById<Button>(R.id.add_blocked_app).setOnClickListener {
            if (!PinManager.ensureAuthorized(requireActivity())) return@setOnClickListener
            startActivity(
                AppPickerActivity.newIntent(requireContext(), AppPickerActivity.BLOCK_TYPE_STANDARD)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshLists()
    }

    private fun refreshLists() {
        renderAppList(alwaysAppsContainer, alwaysAppsEmpty, repository.getAlwaysBlockedApps()) {
            repository.removeAlwaysBlockedApp(it)
        }
        renderAppList(blockedAppsContainer, blockedAppsEmpty, repository.getBlockedApps()) {
            repository.removeBlockedApp(it)
        }
    }

    private fun renderAppList(
        container: LinearLayout,
        emptyView: TextView,
        items: List<String>,
        onRemove: (String) -> Unit
    ) {
        container.removeAllViews()
        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }
        emptyView.visibility = View.GONE
        val inflater = LayoutInflater.from(requireContext())
        items.forEach { item ->
            val row = inflater.inflate(R.layout.list_item_block_app, container, false)
            val labelInfo = AppInfoUtils.resolveAppInfo(requireContext(), item)
            row.findViewById<TextView>(R.id.item_text).text = labelInfo.label
            row.findViewById<android.widget.ImageView>(R.id.item_icon).setImageDrawable(labelInfo.icon)
            row.findViewById<View>(R.id.remove_button).setOnClickListener {
                if (!PinManager.ensureAuthorized(requireActivity())) return@setOnClickListener
                onRemove(item)
                refreshLists()
            }
            container.addView(row)
        }
    }
}
