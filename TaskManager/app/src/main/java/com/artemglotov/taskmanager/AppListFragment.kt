package com.artemglotov.taskmanager

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemglotov.taskmanager.models.App

class AppListFragment : androidx.fragment.app.Fragment() {
    private var listener: AppListener? = null
    private var appsAdapter: AppRecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_list, container, false)

        val isShowMoreButton = arguments?.getBoolean("IS_SHOW_MORE", false) ?: false
        appsAdapter = AppRecyclerViewAdapter(listener, context!!, isShowMoreButton)
        // Set the appsAdapter
        if (view is androidx.recyclerview.widget.RecyclerView) {
            with(view) {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                this.adapter = appsAdapter
            }
        }
        return view
    }

    fun refreshAppList(appList: ArrayList<App>) {
        appsAdapter?.refreshAppList(appList)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement AppListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface AppListener {
        fun onAppClick(item: App)
        fun switchToApp(packageName: String)
        fun endApp(packageName: String)
        fun appInfo(packageName: String)
        fun ignoreApp(packageName: String)
    }
}
