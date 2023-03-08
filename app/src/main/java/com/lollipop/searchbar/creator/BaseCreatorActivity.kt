package com.lollipop.searchbar.creator

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.sidesheet.SideSheetBehavior
import com.google.android.material.slider.Slider
import com.lollipop.filechooser.FileChooseResult
import com.lollipop.filechooser.FileChooser
import com.lollipop.filechooser.FileMime
import com.lollipop.searchbar.SearchBarInfo
import com.lollipop.searchbar.WidgetDBUtil
import com.lollipop.searchbar.WidgetUtil
import com.lollipop.searchbar.databinding.ActivityCreatorBinding
import com.lollipop.searchbar.databinding.ItemAppInfoBinding
import java.io.File

abstract class BaseCreatorActivity : AppCompatActivity() {

    protected abstract val widgetLayoutId: Int

    private val widgetBean = SearchBarInfo(
        id = 0,
        widgetId = 0,
        packageName = "",
        activityName = "",
        icon = "",
        content = "",
        background = 0.7F
    )

    private val binding by lazy {
        ActivityCreatorBinding.inflate(layoutInflater)
    }

    private var widgetView: WidgetUtil.NativeViewInterface? = null

    private var selectedAppInfo: AppInfo? = null

    private val appList = ArrayList<AppInfo>()

    private val fileChooser = FileChooser.registerChooserLauncher(getThis(), ::onFileChooseResult)

    private fun getThis(): BaseCreatorActivity {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appList.addAll(loadAppInfo())
        initView()
    }

    private fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val widget = layoutInflater.inflate(widgetLayoutId, binding.previewGroup, true)
        widgetView = WidgetUtil.NativeViewInterface(widget)

        val sideSheetBehavior = SideSheetBehavior.from(binding.sideSheet)

        binding.alphaSlider.value = widgetBean.background * 100
        binding.labelInputEdit.setText(widgetBean.content)

        binding.alphaSlider.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (fromUser) {
                widgetBean.background = value * 0.01F
                onSearchBarChanged()
            }
        })
        binding.labelInputEdit.addTextChangedListener {
            widgetBean.content = it?.toString() ?: ""
            onSearchBarChanged()
        }
        binding.sideSheet.setOnClickListener {
            binding.sideSheetCloseButton.callOnClick()
        }
        binding.sideSheetCloseButton.setOnClickListener {
            sideSheetBehavior.state = SideSheetBehavior.STATE_HIDDEN
        }
        binding.intentInputEdit.keyListener = null
        binding.intentInputEdit.setOnClickListener {
            sideSheetBehavior.state = SideSheetBehavior.STATE_EXPANDED
        }
        binding.appListView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.appListView.adapter = AppAdapter(appList, ::onAppClick)
        binding.saveButton.setOnClickListener {
            createWidget()
            onBackPressed()
        }
        binding.selectIconChooseBtn.setOnClickListener {
            fileChooser.launch().type(FileMime.Image.ALL).start()
        }
        binding.selectIconClearBtn.setOnClickListener {
            clearIconFile()
            onSearchBarChanged()
        }
        onSearchBarChanged()
    }

    private fun onFileChooseResult(result: FileChooseResult) {
        when (result) {
            FileChooseResult.Empty -> {}
            is FileChooseResult.Multiple -> {
                clearIconFile()
                doAsync {
                    val iconFile = createIconFile()
                    result.save(getThis(), 0, iconFile)
                    onUI {
                        widgetBean.icon = iconFile.path
                        onSearchBarChanged()
                    }
                }
            }
            is FileChooseResult.Single -> {
                clearIconFile()
                doAsync {
                    val iconFile = createIconFile()
                    result.save(getThis(), iconFile)
                    onUI {
                        widgetBean.icon = iconFile.path
                        onSearchBarChanged()
                    }
                }
            }
        }
    }

    private fun clearIconFile() {
        val icon = widgetBean.icon
        if (icon.isEmpty()) {
            return
        }
        widgetBean.icon = ""
        doAsync {
            val file = File(icon)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun createIconFile(): File {
        return File(filesDir, System.currentTimeMillis().toString(16))
    }

    private fun doAsync(callback: () -> Unit) {
        Thread {
            try {
                callback()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }.start()
    }

    protected fun onUI(callback: () -> Unit) {
        runOnUiThread {
            try {
                callback()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onAppClick(appInfo: AppInfo) {
        selectedAppInfo = appInfo
        binding.intentInputEdit.setText(appInfo.label)
        binding.sideSheetCloseButton.callOnClick()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onSearchBarChanged() {
        val view = widgetView ?: return
        WidgetUtil.updateUI(widgetBean, view)
    }

    private fun createWidget() {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return
        }
        val appInfo = selectedAppInfo
        if (appInfo != null) {
            widgetBean.packageName = appInfo.packageName
            widgetBean.activityName = appInfo.activityName
        } else {
            widgetBean.packageName = ""
            widgetBean.activityName = ""
        }
        val dbUtil = WidgetDBUtil(this)
        widgetBean.widgetId = widgetId
        dbUtil.insert(widgetBean)
        dbUtil.close()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        WidgetUtil.update(this, widgetBean, widgetLayoutId, appWidgetManager)
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetBean.widgetId)
        setResult(Activity.RESULT_OK, resultValue)
    }

    private fun loadAppInfo(): List<AppInfo> {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val appList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.MATCH_ALL
            )
        }
        val resultList = ArrayList<AppInfo>()
        for (info in appList) {
            val activityInfo = info.activityInfo
            val packageName = activityInfo.packageName
            resultList.add(
                AppInfo(
                    activityInfo.loadLabel(pm),
                    packageName,
                    activityFullName(packageName, activityInfo.name)
                )
            )
        }
        return resultList
    }

    private fun activityFullName(pkg: String, cls: String): String {
        return if (cls[0] == '.') {
            pkg + cls
        } else {
            cls
        }
    }

    private class AppAdapter(
        private val data: List<AppInfo>,
        private val onAppClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<AppHolder>() {

        private var layoutInflater: LayoutInflater? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
            val inflater = layoutInflater ?: LayoutInflater.from(parent.context)
            layoutInflater = inflater
            return AppHolder(ItemAppInfoBinding.inflate(inflater, parent, false), ::onItemClick)
        }

        private fun onItemClick(position: Int) {
            if (position < 0 || position >= itemCount) {
                return
            }
            onAppClick(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            holder.bind(data[position])
        }

    }

    private class AppHolder(
        private val binding: ItemAppInfoBinding,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onItemClick()
            }
        }

        private fun onItemClick() {
            onClick(adapterPosition)
        }

        fun bind(info: AppInfo) {
            binding.appLabelView.text = info.label
            binding.appIntentView.text = info.activityName
        }

    }

    private class AppInfo(
        val label: CharSequence,
        val packageName: String,
        val activityName: String
    )

}