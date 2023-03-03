package com.lollipop.searchbar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import java.io.File
import kotlin.math.max
import kotlin.math.min

object WidgetUtil {

    fun update(
        context: Context,
        widgetBean: SearchBarInfo,
        layoutId: Int,
        appWidgetManager: AppWidgetManager
    ) {
        val views = RemoteViewInterface(RemoteViews(context.packageName, layoutId))
        updateUI(widgetBean, views)
        //创建点击意图
        val intent = Intent.parseUri(widgetBean.intent, Intent.URI_INTENT_SCHEME)
        //请求ID重复会导致延时意图被覆盖，刷新模式表示覆盖的方式
        val pendingIntent = PendingIntent.getActivity(
            context,
            widgetBean.widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        //为小部件设置点击事件
        views.target.setOnClickPendingIntent(R.id.widgetBody, pendingIntent)
        appWidgetManager.updateAppWidget(widgetBean.widgetId, views.target)
    }

    fun updateUI(widgetBean: SearchBarInfo, views: ViewInterface) {
        views.setAlpha(R.id.backgroundView, widgetBean.background)
        views.setTextViewText(R.id.backgroundView, widgetBean.content)
        views.setImageViewBitmap(R.id.iconView, widgetBean.icon)
    }

    abstract class ViewInterface {
        abstract fun setImageViewBitmap(id: Int, path: String)
        abstract fun setAlpha(id: Int, alpha: Float)
        abstract fun setTextViewText(id: Int, text: CharSequence)

        protected fun getBitmap(path: String): Bitmap? {
            try {
                val image = File(path)
                if (path.isEmpty() || !image.exists()) {
                    return null
                }
                return BitmapFactory.decodeFile(image.path)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }

        protected fun getAlpha(value: Float): Int {
            return min(255, max(0, (value * 255).toInt()))
        }

    }

    class NativeViewInterface(private val views: View) : ViewInterface() {

        private fun <T : View> Int.find(): T? {
            return views.findViewById<T>(this)
        }

        override fun setImageViewBitmap(id: Int, path: String) {
            id.find<ImageView>()?.setImageBitmap(getBitmap(path))
        }

        override fun setAlpha(id: Int, alpha: Float) {
            id.find<View>()?.alpha = alpha
        }

        override fun setTextViewText(id: Int, text: CharSequence) {
            id.find<TextView>()?.text = text
        }
    }

    private class RemoteViewInterface(
        private val views: RemoteViews
    ) : ViewInterface() {

        val target: RemoteViews
            get() {
                return views
            }

        override fun setImageViewBitmap(id: Int, path: String) {
            views.setImageViewBitmap(id, getBitmap(path))
        }

        override fun setAlpha(id: Int, alpha: Float) {
            views.setFloat(id, "setAlpha", alpha)
        }

        override fun setTextViewText(id: Int, text: CharSequence) {
            views.setTextViewText(id, text)
        }

    }

}