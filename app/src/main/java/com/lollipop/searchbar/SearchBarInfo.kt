package com.lollipop.searchbar

data class SearchBarInfo(
    var id: Int,
    var widgetId: Int,
    var packageName: String,
    var activityName: String,
    var icon: String,
    var content: String,
    var background: Float
)