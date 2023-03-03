package com.lollipop.searchbar.provider

import com.lollipop.searchbar.R
import com.lollipop.searchbar.SearchBarWidgetProvider

class SearchBarCenterWidgetProvider : SearchBarWidgetProvider() {
    override val weightLayout: Int
        get() {
            return R.layout.widget_search_bar_center
        }
}