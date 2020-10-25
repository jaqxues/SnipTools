package com.jaqxues.sniptools.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnapTools.<br>
 * Date: 24.10.20 - Time 20:35.
 */
@Entity
data class ServerPackEntity(
    @ColumnInfo(name = "sc_version") val scVersion: String,
    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "dev_pack") val devPack: Boolean = false,
    @ColumnInfo(name = "pack_version") val packVersion: String,
    @ColumnInfo(name = "pack_v_code") val packVersionCode: Int,
    @ColumnInfo(name = "min_apk_v_code") val minApkVersionCode: Int,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int
)