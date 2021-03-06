package com.jaqxues.sniptools.packimpl.hookdec

import com.jaqxues.akrolyb.genhook.decs.MemberDec.ConstructorDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec.MethodDec
import com.jaqxues.sniptools.packimpl.features.*
import com.jaqxues.sniptools.packimpl.hookdec.ClassDeclarations.CBC_ENCRYPTION_ALGORITHM
import com.jaqxues.sniptools.packimpl.hookdec.ClassDeclarations.SCREENSHOT_DETECTOR
import com.jaqxues.sniptools.packimpl.hookdec.ClassDeclarations.SNAP_MODEL
import com.jaqxues.sniptools.packimpl.hookdec.ClassDeclarations.STORY_VIEWED_PLUGIN
import java.io.InputStream

object MemberDeclarations {
    val SCREENSHOT_DETECTED = MethodDec(
        SCREENSHOT_DETECTOR,
        "a",
        arrayOf(ScreenshotBypass::class.java),

        SCREENSHOT_DETECTOR, "Eq6"
    )

    val MARK_STORY_AS_VIEWED = MethodDec(
        STORY_VIEWED_PLUGIN,
        "a0",
        arrayOf(StealthViewing::class.java),

        "aIf", "byf"
    )

    val SNAP_MODEL_CONSTRUCTOR = ConstructorDec(
        SNAP_MODEL,
        arrayOf(UnlimitedViewing::class.java),

        String::class.java, Boolean::class.java, String::class.java, String::class.java,
        String::class.java, Long::class.javaObjectType, "Fe6", Long::class.java,
        Boolean::class.java, Long::class.javaObjectType, Long::class.java
    )

    val DECRYPT_MEDIA_STREAM = MethodDec(
        CBC_ENCRYPTION_ALGORITHM,
        "L0",
        arrayOf(SavingFeature::class.java),

        InputStream::class.java
    )
}