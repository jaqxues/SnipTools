package com.jaqxues.sniptools

import android.app.Application
import com.jaqxues.sniptools.utils.CommonSetup
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 03.06.20 - Time 18:58.
 */
@HiltAndroidApp
class CustomApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        CommonSetup.initTimber()
        Timber.d("Initializing Application")
    }

    companion object {
        const val MODULE_TAG = "SnipTools"

        const val PACKAGE_NAME = BuildConfig.APPLICATION_ID
    }
}