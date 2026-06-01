package com.jaryjay.defender

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class BlockedSitesFragment : Fragment(R.layout.fragment_blocked_sites) {
    private lateinit var repository: BlockListRepository
    private lateinit var alwaysSitesContainer: LinearLayout
    private lateinit var blockedSitesContainer: LinearLayout
    private lateinit var alwaysSitesEmpty: TextView
    private lateinit var blockedSitesEmpty: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = BlockListRepository(requireContext())
        alwaysSitesContainer = view.findViewById(R.id.always_sites_container)
        blockedSitesContainer = view.findViewById(R.id.blocked_sites_container)
        alwaysSitesEmpty = view.findViewById(R.id.always_sites_empty)
        blockedSitesEmpty = view.findViewById(R.id.blocked_sites_empty)

        setupAddButton(view, R.id.add_always_site, R.id.input_always_site) {
            repository.addAlwaysBlockedSite(it)
        }
        setupAddButton(view, R.id.add_blocked_site, R.id.input_blocked_site) {
            repository.addBlockedSite(it)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshLists()
    }

    private fun setupAddButton(root: View, buttonId: Int, inputId: Int, addAction: (String) -> Unit) {
        val button = root.findViewById<Button>(buttonId)
        val input = root.findViewById<EditText>(inputId)
        button.setOnClickListener {
            if (!PinManager.ensureAuthorized(requireActivity())) return@setOnClickListener
            val value = input.text.toString().trim()
            if (value.isBlank()) return@setOnClickListener
            addAction(value)
            input.text.clear()
            refreshLists()
        }
    }

    private fun refreshLists() {
        renderList(alwaysSitesContainer, alwaysSitesEmpty, repository.getAlwaysBlockedSites()) {
            repository.removeAlwaysBlockedSite(it)
        }
        renderList(blockedSitesContainer, blockedSitesEmpty, repository.getBlockedSites()) {
            repository.removeBlockedSite(it)
        }
    }

    private fun renderList(
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
            val row = inflater.inflate(R.layout.list_item_block, container, false)
            val text = row.findViewById<TextView>(R.id.item_text)
            val remove = row.findViewById<View>(R.id.remove_button)
            text.text = item
            remove.setOnClickListener {
                if (!PinManager.ensureAuthorized(requireActivity())) return@setOnClickListener
                onRemove(item)
                refreshLists()
            }
            container.addView(row)
        }
    }
}
