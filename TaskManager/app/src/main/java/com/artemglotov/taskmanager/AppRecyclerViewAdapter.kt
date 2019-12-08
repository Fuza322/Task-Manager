package com.artemglotov.taskmanager


import android.content.Context
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.artemglotov.taskmanager.AppListFragment.AppListener
import com.artemglotov.taskmanager.models.App
import kotlinx.android.synthetic.main.fragment_app.view.*

class AppRecyclerViewAdapter(
    private val listener: AppListener?,
    private val context: Context,
    private val isShowMoreButton: Boolean = false
) : androidx.recyclerview.widget.RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder>() {

    private var apps: ArrayList<App> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.name
        if (isShowMoreButton) {
            holder.moreButton.setOnClickListener {
                val popupMenu = PopupMenu(context, holder.moreButton)
                popupMenu.inflate(R.menu.click_context_menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    val packageName = (holder.view.tag as App).packageName
                    when (item.itemId) {
                        R.id.ccmenu_endApp -> {
                            listener?.endApp(packageName)
                            true
                        }
                        R.id.ccmenu_switchToApp -> {
                            listener?.switchToApp(packageName)
                            true
                        }
                        R.id.ccmenu_appInfo -> {
                            listener?.appInfo(packageName)
                            true
                        }
                        R.id.ccmenu_ignoreApp -> {
                            listener?.ignoreApp(packageName)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        } else {
            holder.moreButton.visibility = View.GONE
        }


        with(holder.view) {
            tag = app
            setOnClickListener {
                val item = it.tag as App
                listener?.onAppClick(item)
            }
        }
    }

    override fun getItemCount(): Int = apps.size

    fun refreshAppList(appList: ArrayList<App>) {
        apps = appList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.app_icon
        val name: TextView = view.app_name
        val moreButton: ImageButton = view.more_button
    }
}
