package com.lollipop.searchbar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

abstract class SearchBarWidgetProvider : AppWidgetProvider() {

    protected abstract val weightLayout: Int

    private fun getDBUtil(context: Context): WidgetDBUtil {
        return WidgetDBUtil(context)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (context == null || appWidgetIds == null || appWidgetManager == null) {
            return
        }

        val dbUtil = getDBUtil(context)
        val infoList = dbUtil.selectAll()
        infoList.forEach { widget ->
            WidgetUtil.update(context, widget, weightLayout, appWidgetManager)
        }
        dbUtil.close()
    }

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        if (context == null || oldWidgetIds == null || newWidgetIds == null || oldWidgetIds.size != newWidgetIds.size) {
            return
        }
        val dbUtil = getDBUtil(context)
        val barInfoList = dbUtil.selectAll()
        for (index in oldWidgetIds.indices) {
            val widgetId = oldWidgetIds[index]
            val widgetInfo = barInfoList.find { it.widgetId == widgetId } ?: continue
            widgetInfo.widgetId = newWidgetIds[index]
            dbUtil.update(widgetInfo)
        }
        dbUtil.close()
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        if (context == null || appWidgetIds == null) {
            return
        }
        val dbUtil = getDBUtil(context)
        for (id in appWidgetIds) {
            dbUtil.deleteByWidgetId(id)
        }
        dbUtil.close()
    }

}