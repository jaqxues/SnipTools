package com.jaqxues.sniptools.packimpl.utils

import com.jaqxues.akrolyb.genhook.FeatureProvider
import com.jaqxues.sniptools.pack.IFeature
import com.jaqxues.sniptools.packimpl.DebugCompat
import com.jaqxues.sniptools.packimpl.PackImpl
import com.jaqxues.sniptools.packimpl.features.*
import com.jaqxues.sniptools.packimpl.hookdec.MemberDeclarations
import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 04.06.20 - Time 00:22.
 */
object FeatureSet : FeatureProvider<IFeature> {
    override val disabledFeatures = { PackImpl.disabledFeatures.value!! }

    override val optionalFeatures = mapOf(
        "chat" to ChatSaving::class,
        "misc" to MiscFeatures::class,
        "saving" to SavingFeature::class,
        "screenshot" to ScreenshotBypass::class,
        "stealth" to StealthViewing::class,
        "unlimited" to UnlimitedViewing::class
    )

    override val forcedFeatures = arrayOf(
        "forced" to ForcedHooks::class,
        DebugCompat.debugFeature?.let { "debug" to it }
    ).filterNotNull().toMap()

    override val hookDefs = arrayOf(
        MemberDeclarations,
        DebugCompat.debugHookDecs
    ).filterNotNull().toTypedArray()
}

// Allow DebugFeature to be included only for debug builds: 2 different implementations
interface IDebugCompat {
    val debugFeature: KClass<out IFeature>?

    val debugPrefsClass: KClass<*>?

    val debugHookDecs: Any?
}
