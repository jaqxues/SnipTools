package com.jaqxues.sniptools.pack

import android.content.Context
import com.jaqxues.akrolyb.pack.ModPackBase
import com.jaqxues.sniptools.data.PackMetadata
import com.jaqxues.sniptools.data.StatefulPackData
import java.io.File
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 03.06.20 - Time 23:58.
 */
object PackLoadManager {
    private val packLoadStates = ConcurrentHashMap<String, StatefulPackData>()

    private fun getMetadata(
        context: Context,
        packFile: File,
        certificate: X509Certificate?,
        packBuilder: PackFactory
    ) = ModPackBase.extractMetadata(context, packFile, certificate, packBuilder)

    suspend fun requestPackMetadata(
        context: Context,
        packFile: File,
        certificate: X509Certificate? = null,
        packBuilder: PackFactory = PackFactory(false)
    ): PackMetadata {
        if (packLoadStates.containsKey(packFile.name)) return packLoadStates.getValue(packFile.name).packMetadata

        try {
            val metadata = getMetadata(context, packFile, certificate, packBuilder)
            packLoadStates[packFile.name] = StatefulPackData.AvailablePack(packFile, metadata)
            return metadata
        } catch (t: Throwable) {
            packLoadStates[packFile.name] = StatefulPackData.CorruptedPack(packFile, t.message!!)
            throw t
        }
    }

    suspend fun requestLoadPack(
        context: Context,
        packFile: File,
        certificate: X509Certificate? = null,
        packBuilder: PackFactory
    ): ModPack {
        val metadata = try {
            getMetadata(context, packFile, certificate, packBuilder)
        } catch (t: Throwable) {
            packLoadStates[packFile.name] = StatefulPackData.CorruptedPack(packFile, t.message!!)
            throw t
        }

        try {
            val pack: ModPack = ModPackBase.buildPack(context, packFile, certificate, packBuilder)
            packLoadStates[packFile.name] = StatefulPackData.LoadedPack(packFile, metadata)
            return pack
        } catch (t: Throwable) {
            packLoadStates[packFile.name] = StatefulPackData.PackLoadError(packFile, metadata, t.message!!)
            throw t
        }
    }
}