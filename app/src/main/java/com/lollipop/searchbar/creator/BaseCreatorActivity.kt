package com.lollipop.searchbar.creator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.slider.Slider
import com.lollipop.searchbar.SearchBarInfo
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
        val widget = layoutInflater.inflate(widgetLayoutId, binding.previewGroup, true)
        widgetView = WidgetUtil.NativeViewInterface(widget)
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

        onSearchBarChanged()
    }

    private fun onSearchBarChanged() {
        val view = widgetView ?: return
        WidgetUtil.updateUI(widgetBean, view)
    }

}