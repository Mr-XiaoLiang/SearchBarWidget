package com.lollipop.searchbar

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.searchbar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator)
//        updateWidget()
    }

    private fun updateWidget() {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val dbUtil = WidgetDBUtil(this)
        val widgetBean = SearchBarInfo(0, widgetId, "", "", "HelloWorld", 0.5F)
        dbUtil.insert(widgetBean)
        dbUtil.close()

        val appWidgetManager = AppWidgetManager.getInstance(this)

        WidgetUtil.update(this, widgetBean, R.layout.widget_search_bar_bottom, appWidgetManager)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetBean.widgetId)
        setResult(Activity.RESULT_OK, resultValue)
    }
}