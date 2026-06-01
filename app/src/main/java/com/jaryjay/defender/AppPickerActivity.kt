package com.jaryjay.defender

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class AppPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        val toolbar = findViewById<MaterialToolbar>(R.id.app_picker_toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val blockType = intent.getStringExtra(EXTRA_BLOCK_TYPE) ?: BLOCK_TYPE_STANDARD
        toolbar.title = if (blockType == BLOCK_TYPE_ALWAYS) {
            getString(R.string.add_always_blocked_app)
        } else {
            getString(R.string.add_blocked_app)
        }
        val repository = BlockListRepository(this)
        val listView = findViewById<ListView>(R.id.app_list)
        val apps = AppInfoUtils.loadLaunchableApps(this)
        listView.adapter = AppListAdapter(this, apps)
        listView.setOnItemClickListener { _, _, position, _ ->
            if (!PinManager.ensureAuthorized(this)) return@setOnItemClickListener
            val app = apps[position]
            if (blockType == BLOCK_TYPE_ALWAYS) {
                repository.addAlwaysBlockedApp(app.packageName)
                Toast.makeText(this, getString(R.string.app_added_to_always), Toast.LENGTH_SHORT).show()
            } else {
                repository.addBlockedApp(app.packageName)
                Toast.makeText(this, getString(R.string.app_added_to_blocked), Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_BLOCK_TYPE = "block_type"
        const val BLOCK_TYPE_ALWAYS = "always"
        const val BLOCK_TYPE_STANDARD = "standard"

        fun newIntent(context: Context, blockType: String): Intent {
            return Intent(context, AppPickerActivity::class.java).putExtra(EXTRA_BLOCK_TYPE, blockType)
        }
    }
}
