package com.jaqxues.sniptools.packimpl.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jaqxues.sniptools.packimpl.utils.PackPreferences.DISABLE_CAPTION_LENGTH_LIMIT
import com.jaqxues.sniptools.packimpl.utils.SwitchPreference
import com.jaqxues.sniptools.packimpl.utils.TitleAndDescription

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 26.09.20 - Time 21:43.
 */
@Composable
fun MiscChangesScreen() {
    Column(Modifier.padding(16.dp)) {
        SwitchPreference(DISABLE_CAPTION_LENGTH_LIMIT) {
            TitleAndDescription(
                title = "Disable Caption Limit",
                description = "Disables the character limit for captions on Snaps"
            )
        }
    }
}
