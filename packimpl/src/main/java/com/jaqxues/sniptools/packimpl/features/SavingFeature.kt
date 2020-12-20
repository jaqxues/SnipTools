package com.jaqxues.sniptools.packimpl.features

import android.content.Context
import com.jaqxues.akrolyb.genhook.decs.AddInsField
import com.jaqxues.akrolyb.genhook.decs.after
import com.jaqxues.akrolyb.prefs.getPref
import com.jaqxues.sniptools.pack.IFeature
import com.jaqxues.sniptools.packimpl.fragment.Destinations
import com.jaqxues.sniptools.packimpl.hookdec.MemberDeclarations
import com.jaqxues.sniptools.packimpl.utils.PackPreferences.AUTO_SAVE_SNAPS
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 24.08.20 - Time 13:12.
 */
class SavingFeature : IFeature() {
    override fun getDestinations() = arrayOf(Destinations.SAVING.destination)

    override fun loadFeature(classLoader: ClassLoader, context: Context) {
        if (AUTO_SAVE_SNAPS.getPref()) {
            val isVideoAddIns = AddInsField<Boolean>()
            findAndHookMethod("VO5", classLoader, "v", "Kb6 ", after {
                it.result?.let { result ->
                    val isVideo = when (val type = getObjectField(it.args[0], "b").toString()) {
                        "IMAGE" -> false
                        "VIDEO", "VIDEO_NO_SOUND" -> true
                        else -> {
                            Timber.d("Unsupported Media Type %s", type)
                            return@let
                        }
                    }
                    isVideoAddIns.set(result, isVideo)
                }
            })

            hookMethod(MemberDeclarations.DECRYPT_MEDIA_STREAM, after {
                val ext = if (!(isVideoAddIns.get(it.thisObject) ?: return@after)) "jpg" else "mp4"
                val result = it.result as InputStream
                val bytes = result.use(InputStream::readBytes)

                val folder = File("/storage/emulated/0/SnipTools/Test").apply {
                    if (!exists()) mkdirs()
                }
                File(folder, "Auto_Save_${System.currentTimeMillis()}_${System.nanoTime()}.$ext")
                    .apply {
                        createNewFile()
                        writeBytes(bytes)
                    }

                it.result = ByteArrayInputStream(bytes)
            })
        }
    }
}
