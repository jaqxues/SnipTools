package com.jaqxues.sniptools.utils

import com.jaqxues.sniptools.data.PackMetadata
import java.io.File
import java.util.jar.Attributes
import java.util.jar.JarFile


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 05.06.20 - Time 13:04.
 */
object PackUtils {
    fun getPackMetadata(file: File) =
        JarFile(file).use {
            it.manifest.mainAttributes.buildMetadata(file)
        }
}

fun Attributes.buildMetadata(file: File): PackMetadata {
    fun getOrThrow(name: String) =
        getValue(name) ?: throw IllegalStateException("Pack did not include \"$name\" Attribute")

    return PackMetadata(
        flavour = getOrThrow("Flavour"),
        scVersion = getOrThrow("ScVersion"),
        name = file.name.dropLast(4),
        devPack = getOrThrow("Development").equals("true", false),
        packVersion = getOrThrow("PackVersion"),
        packVersionCode = getOrThrow("PackVersionCode").toInt(),
        packImplClass = getOrThrow("PackImpl"),
        minApkVersionCode = getOrThrow("MinApkVersionCode").toInt()
    )
}