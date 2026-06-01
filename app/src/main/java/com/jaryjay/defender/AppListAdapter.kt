package com.jaryjay.defender

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class AppListAdapter(
    private val context: Context,
    private val apps: List<AppInfoUtils.AppInfo>
) : BaseAdapter() {
    override fun getCount(): Int = apps.size

    override fun getItem(position: Int): Any = apps[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_app_picker, parent, false)
        val app = apps[position]
        view.findViewById<ImageView>(R.id.app_icon).setImageDrawable(app.icon)
        view.findViewById<TextView>(R.id.app_name).text = app.label
        return view
    }
}
