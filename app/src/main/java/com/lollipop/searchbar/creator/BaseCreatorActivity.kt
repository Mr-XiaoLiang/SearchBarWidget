package com.lollipop.searchbar.creator

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.sidesheet.SideSheetBehavior
import com.google.android.material.slider.Slider
import com.lollipop.searchbar.SearchBarInfo
import com.lollipop.searchbar.WidgetDBUtil
import com.lollipop.searchbar.WidgetUtil
import com.lollipop.searchbar.databinding.ActivityCreatorBinding

abstract class BaseCreatorActivity : AppCompatActivity() {

    protected abstract val widgetLayoutId: Int

    private val widgetBean = SearchBarInfo(0, 0, "", "", "", 0.7F)

    private val binding by lazy {
        ActivityCreatorBinding.inflate(layoutInflater)
    }

    private var widgetView: WidgetUtil.NativeViewInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
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

        onSearchBarChanged()
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

    private fun updateWidget() {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return
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

}