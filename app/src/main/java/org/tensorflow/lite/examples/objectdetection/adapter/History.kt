package org.tensorflow.lite.examples.objectdetection.adapter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A basic class representing an entity that is a row in a one-column database table.
 *
 * @ Entity - You must annotate the class as an entity and supply a table name if not class name.
 * @ PrimaryKey - You must identify the primary key.
 * @ ColumnInfo - You must supply the column name if it is different from the variable name.
 *
 * See the documentation for the full rich set of annotations.
 * https://developer.android.com/topic/libraries/architecture/room.html
 */

@Entity(tableName = "history_table")
data class History(@PrimaryKey @ColumnInfo(name = "history") val date: String, val productName: String, val result: String, val imgName: String)

//data class History(
//    val date: String,
//    val productName: String,
//    val result: String
//)
