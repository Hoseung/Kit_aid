package org.tensorflow.lite.examples.objectdetection.adapter

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models_table")
data class Models(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "product") val product: String?,
    @ColumnInfo(name = "lot") val lot: Int?,
    @ColumnInfo(name = "hash") val hash: String?,
    @ColumnInfo(name = "uri") val uri: String?
)