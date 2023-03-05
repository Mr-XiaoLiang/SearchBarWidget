package com.lollipop.searchbar

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/**
 * 小部件的数据库操作类
 */
class WidgetDBUtil(
    context: Context
) : DatabaseHelper(context, DB_NAME, null, VERSION) {

    companion object {

        private const val DB_NAME = "WidgetDatabase"
        private const val VERSION = 8

    }

    override fun getTableArray(): Array<Table> {
        return arrayOf(SearchBar)
    }

    fun selectAll(): List<SearchBarInfo> {
        return SearchBar.selectAll(readDb)
    }

    fun update(info: SearchBarInfo) {
        SearchBar.update(writeDb, info)
    }

    fun deleteByWidgetId(widgetId: Int) {
        SearchBar.deleteByWidgetId(writeDb, widgetId)
    }

    fun insert(info: SearchBarInfo) {
        SearchBar.insert(writeDb, info)
    }

    private object SearchBar : Table() {
        override val tableName: String = "SEARCH_BAR_TABLE"
        override val columns: Array<out ColumnEnum>
            get() = Columns.values()

        enum class Columns(override val format: ColumnFormat) : ColumnEnum {
            WIDGET_ID(ColumnFormat.INTEGER),
            PKG(ColumnFormat.TEXT),
            ACTIVITY(ColumnFormat.TEXT),
            ICON(ColumnFormat.TEXT),
            CONTENT(ColumnFormat.TEXT),
            BACKGROUND(ColumnFormat.FLOAT),
        }

        private fun querySearchBarInfoList(
            db: SQLiteDatabase,
            rawQueryCallback: (SQLiteDatabase) -> Cursor,
        ): List<SearchBarInfo> {
            return queryList(db, rawQueryCallback) {
                SearchBarInfo(
                    id = it.getIntByName(idColumn),
                    widgetId = it.getIntByName(Columns.WIDGET_ID),
                    packageName = it.getTextByName(Columns.PKG),
                    activityName = it.getTextByName(Columns.ACTIVITY),
                    icon = it.getTextByName(Columns.ICON),
                    content = it.getTextByName(Columns.CONTENT),
                    background = it.getFloatByName(Columns.BACKGROUND),
                )
            }
        }

        fun selectAll(db: SQLiteDatabase): List<SearchBarInfo> {
            return querySearchBarInfoList(db) {
                db.query(
                    tableName,
                    columnsName,
                    null,
                    null,
                    null,
                    null,
                    "$idColumn DESC"
                )
            }
        }

        fun insert(db: SQLiteDatabase, info: SearchBarInfo): Int {
            val insert = db.insert(
                tableName,
                null,
                getContentValues(info)
            )
            if (insert >= 0) {
                return selectLastId(tableName, idColumn, db)
            }
            return 0
        }

        fun update(db: SQLiteDatabase, info: SearchBarInfo) {
            db.update(
                tableName,
                getContentValues(info),
                "$idColumn = ?",
                arrayOf(info.id.toString())
            )
        }

        private fun getContentValues(info: SearchBarInfo): ContentValues {
            return ContentValues().apply {
                putValue(Columns.WIDGET_ID, info.widgetId)
                putValue(Columns.ICON, info.icon)
                putValue(Columns.PKG, info.packageName)
                putValue(Columns.ACTIVITY, info.activityName)
                putValue(Columns.CONTENT, info.content)
                putValue(Columns.BACKGROUND, info.background)
            }
        }

        fun deleteByWidgetId(db: SQLiteDatabase, id: Int) {
            db.delete(
                tableName,
                "${Columns.WIDGET_ID.name} = ?",
                arrayOf(id.toString())
            )
        }

    }


}