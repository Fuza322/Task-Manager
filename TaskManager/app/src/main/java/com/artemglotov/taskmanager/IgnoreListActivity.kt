package com.artemglotov.taskmanager


import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.artemglotov.taskmanager.models.App

class IgnoreListActivity : AppCompatActivity(), AppListFragment.AppListener {
    override fun switchToApp(packageName: String) {}

    override fun endApp(packageName: String) {}

    override fun appInfo(packageName: String) {}

    override fun ignoreApp(packageName: String) {}

    private lateinit var appListFragment: AppListFragment
    var ignoreList = arrayListOf<App>()
    lateinit var ignoreArray: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ignore_list)

        actionBar?.setDisplayHomeAsUpEnabled(true)


        appListFragment = AppListFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.ignore_list_container, appListFragment)
            .commit()

        refreshIgnoreList()
    }

//    @Override
//    fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.getItemId()) {
//            android.R.id.home -> {
//                // app icon in action bar clicked; go home
//                val intent = Intent(this, TaskManagerActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                startActivity(intent)
//                return true
//            }
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }

    private fun refreshIgnoreList() {
        ignoreArray = getSharedPreferences(IGNORE_ARRAY, 0)
        ignoreList.clear()

        for (pkgName in ignoreArray.all.keys) {
            try {
                val icon = packageManager.getApplicationIcon(packageManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA))
                val title = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA))
                if (title != null) {
                    val app = App(title.toString(), pkgName, icon!!)
                    ignoreList.add(app)
                }
            } catch (e: Exception) {}
        }
        appListFragment.refreshAppList(ignoreList)
    }

    override fun onAppClick(item: App) {
        ignoreArray.edit().apply{
            remove(item.packageName)
            apply()
        }
        refreshIgnoreList()
        Toast.makeText(this, "App removed from ignore list!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val IGNORE_ARRAY = "ignore_array"
    }
}