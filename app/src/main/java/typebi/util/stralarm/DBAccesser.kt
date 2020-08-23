@file:Suppress("SpellCheckingInspection")

package typebi.util.stralarm

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class DBAccesser(private val DB : SQLiteDatabase) {
    private val tableName = "STRALARM"
    private val selectAllStatement = "select * from STRALARM"
    private val updateWhereClause = "NUM = ?"
    fun createTable(createStatement : String){
        DB.execSQL(createStatement)
    }
    fun selectAlarms(): Cursor {
        return DB.rawQuery(selectAllStatement, null)
    }
    fun insertAlarm(insertedData : Intent, selectLatestRow : String) : DTO{
        DB.insert(tableName,null, makeContentValues(insertedData))
        DB.rawQuery(selectLatestRow, null).use { selected ->
            selected.moveToNext()
            return DTO(selected.getInt(0),selected.getString(1),selected.getInt(2),selected.getInt(3),selected.getInt(4),selected.getInt(5),selected.getInt(6),selected.getInt(7))
        }
    }
    fun updateAlarm(insertedData : Intent){
        DB.update(tableName,makeContentValues(insertedData), updateWhereClause, arrayOf(insertedData.getIntExtra("num",-1).toString()))
    }
    fun updateAlarm(insertedData : DTO){
        DB.update(tableName,makeContentValues(insertedData), updateWhereClause, arrayOf(insertedData.num.toString()))
    }
    fun deleteAlarm(insertedData : Intent){
        DB.delete(tableName,updateWhereClause,arrayOf(insertedData.getIntExtra("num",-1).toString()))
    }
    private fun makeContentValues(insertData : Intent) :ContentValues{
        return ContentValues().apply {
            put("NAME", insertData.getStringExtra("name"))
            put("START_H", insertData.getIntExtra("startHour", 0))
            put("START_M", insertData.getIntExtra("startMin", 0))
            put("END_H", insertData.getIntExtra("endHour", 0))
            put("END_M", insertData.getIntExtra("endMin", 0))
            put("INTERVAL", insertData.getIntExtra("intvl", 0))
            put("SETTINGS",insertData.getIntExtra("settings", 0))
        }
    }
    private fun makeContentValues(insertData : DTO) :ContentValues{
        return ContentValues().apply {
            put("NAME", insertData.name)
            put("START_H", insertData.startHour)
            put("START_M", insertData.startMin)
            put("END_H", insertData.endHour)
            put("END_M", insertData.endMin)
            put("INTERVAL", insertData.interval)
            put("SETTINGS",insertData.settings)
        }
    }
}