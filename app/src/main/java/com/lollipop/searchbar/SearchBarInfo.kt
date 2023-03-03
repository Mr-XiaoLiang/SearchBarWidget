package com.lollipop.searchbar

import android.content.Intent

data class SearchBarInfo(
    var id: Int,
    var widgetId: Int,
    var intent: String,
    var icon: String,
    var content: String,
    var background: Float
) {

    fun parseIntent(): Intent {
        return Intent.parseUri(intent, Intent.URI_INTENT_SCHEME)
    }

    fun saveIntent(i: Intent) {
        intent = i.toUri(Intent.URI_INTENT_SCHEME)
    }

}