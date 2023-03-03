package com.lollipop.searchbar.provider

import com.lollipop.searchbar.R
import com.lollipop.searchbar.SearchBarWidgetProvider

class SearchBarTopWidgetProvider : SearchBarWidgetProvider() {
    override val weightLayout: Int
        get() {
            return R.layout.widget_search_bar_top
        }
}