package com.artemglotov.taskmanager

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.artemglotov.taskmanager.models.App
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*

class MainActivity : AppCompatActivity(), AppListFragment.AppListener {
    override fun switchToApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        applicationContext.startActivity(intent)
    }

    override fun endApp(packageName: String) {
        if (packageName == "com.artemglotov.taskmanager") {
            this.finish()
        }
        killApp(packageName)
        refreshAppList()
        Toast.makeText(applicationContext, "App killed!", Toast.LENGTH_SHORT).show()
    }

    override fun appInfo(packageName: String) {
        getAppInfo(packageName)
    }

    override fun ignoreApp(packageName: String) {
        addToIgnore(packageName)
        refreshAppList()
    }

    private lateinit var appListFragment: AppListFragment
    private var ignoreList = ArrayList<String>()

    lateinit var ignoreArray: SharedPreferences
    lateinit var preferences: SharedPreferences
    private var totalMemory: Float = 0.0f

    private lateinit var touchAction: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ignoreArray = getSharedPreferences(IGNORE_ARRAY, 0)
        ignoreList.addAll(ignoreArray.all.keys)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        touchAction = preferences.getString("touch_action", "Show All") ?: "Show All"

        kill_all_button.setOnClickListener {
            endAllTask(getAppsList())
        }

        appListFragment = AppListFragment()
        appListFragment.arguments = Bundle().apply {
            putBoolean("IS_SHOW_MORE", true)
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.app_list_container, appListFragment)
            .commit()

        refreshAppList()
        refreshMemoryInfo()
    }


    override fun onStart() {
        super.onStart()
        refreshAppList()
    }

    override fun onResume() {
        touchAction = preferences.getString("touch_action", "Show All") ?: "Show All"

        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_refresh -> {
                refreshAppList()
                return true
            }
            R.id.settings -> {
                val t = Intent(this, SettingsActivity::class.java)
                startActivity(t)
                return true
            }
            R.id.edit_ignore -> {
                val intent = Intent(this, IgnoreListActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.exit -> {
                this.finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getAppsList(): ArrayList<App> {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val listOfProcesses = activityManager.runningAppProcesses
        val appsList = ArrayList<App>()
        for (process in listOfProcesses) {
            if (packageManager.getLaunchIntentForPackage(process.processName) != null && !checkIfIgnore(process.processName)) {
                try {
                    val info = packageManager.getApplicationInfo(process.processName, PackageManager.GET_META_DATA)
                    val appIcon = packageManager.getApplicationIcon(info)
                    val appName = packageManager.getApplicationLabel(info)
                    val app = App(appName.toString(), process.processName, appIcon)
                    appsList.add(app)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
        return appsList
    }

    private fun refreshAppList() {
        val appList = getAppsList()
        appListFragment.refreshAppList(appList)
        checkNoAppsRunning(appList)
        refreshMemoryInfo()
    }

    private fun checkNoAppsRunning(appsList: ArrayList<App>) {
        if (appsList.count() > 0) {
            app_list_container.visibility = View.VISIBLE
            kill_all_button.isEnabled = true
            no_apps_text.visibility = View.GONE
        } else {
            no_apps_text.visibility = View.VISIBLE
            app_list_container.visibility = View.INVISIBLE
            kill_all_button.isEnabled = false
        }
    }


    override fun onAppClick(item: App) {
        val packageName = item.packageName
        when (touchAction) {
            "End App" -> {
                if (packageName == "com.artemglotov.taskmanager") {
                    this.finish()
                }
                killApp(packageName)
                refreshAppList()
                Toast.makeText(applicationContext, "App killed!", Toast.LENGTH_SHORT).show()
            }
            "Switch To App" -> {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                applicationContext.startActivity(intent)
            }
            "App Info" -> getAppInfo(packageName)
            "Ignore App" -> addToIgnore(packageName)
            // TODO check it
            else -> {}//parent.showContextMenuForChild(view)
        }
        refreshAppList()
    }

    private fun getAppInfo(pkgName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = Uri.parse("package:$pkgName")
        startActivity(intent)
    }

    private fun addToIgnore(apps: List<App>) {
        for (app in apps) {
            val pkgName = app.packageName
            addToIgnore(pkgName)
        }
        refreshAppList()
        Toast.makeText(applicationContext, "${apps.count()} apps added to ignore list.", Toast.LENGTH_SHORT).show()
    }

    private fun addToIgnore(pkgName: String) {
        ignoreArray.edit(). apply {
            putString(pkgName, pkgName)
            apply()
        }
        ignoreList.add(pkgName)
    }

    private fun killApp(pkgName: String) {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.restartPackage(pkgName)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val pkgName = info.targetView.tag as String

        when (item.itemId) {
            R.id.ccmenu_endApp -> {
                if (pkgName == "com.artemglotov.taskmanager") {
                    this.finish()
                }
                killApp(pkgName)
                refreshAppList()
                Toast.makeText(applicationContext, "App killed!", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.ccmenu_switchToApp -> {
                val intent = packageManager.getLaunchIntentForPackage(pkgName)
                applicationContext.startActivity(intent)
                return true
            }
            R.id.ccmenu_appInfo -> {
                getAppInfo(pkgName)
                addToIgnore(pkgName)
                refreshAppList()
                return super.onContextItemSelected(item)
            }
            R.id.ccmenu_ignoreApp -> {
                addToIgnore(pkgName)
                refreshAppList()
                return super.onContextItemSelected(item)
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    private fun endAllTask(vararg names: List<App>) {
        GlobalScope.launch(Dispatchers.IO) {
            val apps = names[names.size - 1]
            for (app in apps) {
                killApp(app.packageName)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "All apps killed!", Toast.LENGTH_SHORT).show()
                refreshAppList()
                refreshMemoryInfo()
            }
        }
    }

    private fun refreshMemoryInfo() {
        if (totalMemory.equals(0.0f)) {
            try {
                val reader = RandomAccessFile("/proc/meminfo", "r")
                val load = reader.readLine()
                val memInfo = load.split(" ")
                totalMemory = memInfo[9].toFloat() / 1024
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }

        val activityManager = getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val availableMemory = memoryInfo.availMem / 1048576L
        memory_info_text.text = getString(R.string.memory_template, availableMemory.toInt(), totalMemory.toInt())
        val progress = ((totalMemory - availableMemory) / totalMemory * 100).toInt()
        available_memory_progress.progress = progress
    }

    private fun checkIfIgnore(pkgName: String): Boolean {
        return ignoreList.contains(pkgName)
    }

    companion object {
        const val IGNORE_ARRAY = "ignore_array"
    }
}
