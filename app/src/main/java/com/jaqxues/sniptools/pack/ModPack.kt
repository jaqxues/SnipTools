package com.jaqxues.sniptools.pack

import android.content.Context
import com.jaqxues.akrolyb.genhook.FeatureHelper
import com.jaqxues.akrolyb.genhook.FeatureManager
import com.jaqxues.akrolyb.pack.AppData
import com.jaqxues.akrolyb.pack.ModPackBase
import com.jaqxues.akrolyb.pack.PackFactoryBase
import com.jaqxues.sniptools.BuildConfig
import com.jaqxues.sniptools.data.PackMetadata
import com.jaqxues.sniptools.fragments.PackFragment
import com.jaqxues.sniptools.utils.buildMetadata
import com.jaqxues.sniptools.utils.installedScVersion
import java.io.File
import java.util.jar.Attributes


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 03.06.20 - Time 23:58.
 */
abstract class ModPack(metadata: PackMetadata) : ModPackBase<PackMetadata>(metadata) {
    abstract val featureManager: FeatureManager<out IFeature>
    abstract val staticFragments: List<PackFragment>
    abstract val lateInitActivity: String
}

abstract class IFeature: FeatureHelper() {
    abstract fun getFragments(): Array<out PackFragment>
}

class PackFactory(private val checkScVersion: Boolean): PackFactoryBase<PackMetadata>() {
    override val appData = AppData(BuildConfig.VERSION_CODE, BuildConfig.DEBUG)

    override fun buildMeta(attributes: Attributes, context: Context, file: File) =
        attributes.buildMetadata(file)

    override fun performChecks(packMetadata: PackMetadata, context: Context, file: File) {
        super.performChecks(packMetadata, context, file)

        if (checkScVersion) {
            val scVersion = context.installedScVersion
            val supportedScVersion = packMetadata.scVersion
            if (scVersion != supportedScVersion)
                throw UnsupportedScVersion(scVersion, supportedScVersion)
        }
    }
}

class UnsupportedScVersion(scVersion: String?, supportedScVersion: String):
    Exception("Current Snapchat Version not supported ('$scVersion' - '$supportedScVersion')")
