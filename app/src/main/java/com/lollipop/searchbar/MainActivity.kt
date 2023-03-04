package com.lollipop.searchbar

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.searchbar.creator.BaseCreatorActivity
import com.lollipop.searchbar.databinding.ActivityMainBinding

class MainActivity : BaseCreatorActivity() {

    override val widgetLayoutId: Int
        get() = R.layout.widget_search_bar_center

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_creator)
////        updateWidget()
//    }

}