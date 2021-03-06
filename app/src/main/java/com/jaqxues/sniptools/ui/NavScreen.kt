package com.jaqxues.sniptools.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jaqxues.sniptools.R
import com.jaqxues.sniptools.pack.DestinationIcon
import com.jaqxues.sniptools.utils.Either


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 02.11.20 - Time 19:48.
 */
sealed class LocalScreen(override val route: String, @StringRes val stringRes: Int, _icon: Any?) :
    CheckIconTypes(_icon), NavScreen {
    object Home : LocalScreen("home", R.string.menu_home, Icons.Default.Home)
    object PackManager : LocalScreen("pack_manager", R.string.menu_packs, R.drawable.ic_pack)
    object KnownBugs : LocalScreen("known_bugs", R.string.menu_bugs, null) {
        override val isTopLevelScreen = false
    }
    object PackHistory: LocalScreen("pack_history", R.string.menu_pack_history, null) {
        override val isTopLevelScreen = false
    }

    object Settings : LocalScreen("settings", R.string.menu_settings, Icons.Default.Settings)
    object Faqs : LocalScreen("faqs", R.string.menu_faqs, R.drawable.ic_question_answer_black_48dp)
    object Support :
        LocalScreen("support", R.string.menu_support, R.drawable.ic_support_agent_black_48dp)

    object AboutUs : LocalScreen("about_us", R.string.menu_about_us, R.drawable.ic_group_black_48dp)
    object Shop : LocalScreen("shop", R.string.menu_shop, R.drawable.ic_payment_black_48dp)
    object Features : LocalScreen("features", R.string.menu_features, Icons.Default.List)
    object Legal : LocalScreen("legal", R.string.menu_legal, Icons.Default.Info)

    override val screenName @Composable get() = stringResource(stringRes)

    companion object {
        val topLevelScreens = arrayOf(
                Home, PackManager, Settings, Faqs,
                Support, AboutUs, Shop, Features, Legal
            )

        val allScreens = arrayOf(
                Home, PackManager, KnownBugs, PackHistory, Settings, Faqs, Support,
                AboutUs, Shop, Features, Legal
            )
    }
}

abstract class CheckIconTypes(private val _icon: Any?) {
    init {
        checkType(_icon)
    }

    val icon: DestinationIcon @Composable get() = getFor(_icon = _icon)

    companion object {
        fun checkType(_icon: Any?) {
            check(_icon == null || _icon is ImageVector || _icon is @DrawableRes Int) {
                "Unknown Type for Icon ('${_icon?.javaClass?.name}' - '${_icon}')"
            }
        }

        @Composable
        fun getFor(_icon: Any?): DestinationIcon = when (_icon) {
            is @DrawableRes Int -> Either.Left(painterResource(_icon))
            is ImageVector -> Either.Right(_icon)
            is Unit -> error("Icon was not specified (Unit)")
            else -> error("Unknown Type for Icon")
        }
    }
}

interface NavScreen {
    val route: String
    @get:Composable
    val screenName: String

    @get:Composable
    val icon: DestinationIcon?

    val isTopLevelScreen: Boolean get() = true
}