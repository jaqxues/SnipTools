package com.jaqxues.sniptools.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jaqxues.sniptools.R
import com.jaqxues.sniptools.pack.PackFactory
import com.jaqxues.sniptools.pack.PackMetadata
import com.jaqxues.sniptools.pack.StatefulPackData
import com.jaqxues.sniptools.ui.LocalScreen
import com.jaqxues.sniptools.ui.composables.EmptyScreenMessage
import com.jaqxues.sniptools.ui.viewmodel.PackViewModel

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 06.09.20 - Time 19:30.
 */

@Composable
fun PackSelectorTab(
    navController: NavController,
    packViewModel: PackViewModel,
    selectedPack: String? = null
) {
    LocalContext.current.let { ctx ->
        LaunchedEffect(null) {
            packViewModel.refreshLocalPacks(ctx, null, PackFactory(false))
        }
    }
    val localPacks = packViewModel.localPacks.observeAsState()
    val localPacksCaptured = localPacks.value

    if (localPacksCaptured.isNullOrEmpty()) {
        EmptyScreenMessage("No Packs found")
    } else {
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            items(localPacksCaptured) {
                val packData by packViewModel.getStateDataForPack(it).run {
                    observeAsState(value!!)
                }
                val initiallyExpanded = selectedPack != null
                        && selectedPack == runCatching { packData.packMetadata.name }.getOrNull()
                PackElementLayout(packData, packViewModel, initiallyExpanded, navController)
            }
        }
    }
}

@Composable
fun PackElementLayout(
    packData: StatefulPackData,
    packViewModel: PackViewModel,
    initiallyExpanded: Boolean,
    navController: NavController
) {
    when (packData) {
        is StatefulPackData.CorruptedPack -> ExpandablePackLayout(
            packName = packData.packFile.name,
            color = Color.Red
        ) {
            Text(
                packData.message,
                Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                fontSize = 12.sp
            )
        }
        else -> {
            val color by animateColorAsState(when (packData) {
                is StatefulPackData.AvailablePack ->
                    Color.White
                is StatefulPackData.LoadedPack ->
                    Color(0xFF00AA00)
                is StatefulPackData.PackLoadError ->
                    Color.Red
                else -> error("Illegal PackLoad State")
            }
            )

            ExpandablePackLayout(
                packName = packData.packMetadata.name,
                color = color,
                initiallyExpanded
            ) {
                Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Divider(Modifier.padding(horizontal = 20.dp))

                    val context = LocalContext.current

                    LocalActionRow(packData, onChangelog = {
                        navController.navigate(
                            "%s/%s/%s".format(
                                LocalScreen.KnownBugs.route,
                                packData.packMetadata.scVersion,
                                packData.packMetadata.packVersion
                            )
                        )
                    }, onChangeActive = {
                        if (it) {
                            packViewModel.activatePack(
                                context,
                                packData.packFile,
                                null,
                                PackFactory(false)
                            )
                        } else {
                            packViewModel.deactivatePack(
                                context,
                                packData.packFile,
                                null,
                                PackFactory(false)
                            )
                        }
                    }, onDelete = {
                        packViewModel.deletePack(packData.packFile)
                    })
                    Divider(Modifier.padding(horizontal = 80.dp))

                    Spacer(Modifier.padding(8.dp))
                    PackMetadataLayout(metadata = packData.packMetadata)

                    if (packData is StatefulPackData.PackLoadError) {
                        Text(packData.message, Modifier.padding(vertical = 8.dp), fontSize = 12.sp)
                    } else {
                        Spacer(Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LocalActionRow(
    packData: StatefulPackData,
    modifier: Modifier = Modifier,
    onChangelog: () -> Unit,
    onChangeActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onChangelog) {
            Image(
                painterResource(id = R.drawable.ic_baseline_bug_report_48),
                "Known Bugs",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.height(24.dp)
            )
        }

        Switch(
            packData.isActive,
            onCheckedChange = onChangeActive,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (packData !is StatefulPackData.PackLoadError)
                    Color(0xFF00AA00) else Color.Red
            ),
            modifier = Modifier.padding(horizontal = 16.dp).size(50.dp)
        )

        IconButton(onClick = onDelete) {
            Image(
                painterResource(id = R.drawable.ic_baseline_remove_circle_outline_48),
                "Remove Pack",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
fun PackMetadataLayout(metadata: PackMetadata) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text("Pack Type: ${if (metadata.devPack) "Developer" else "User"}", fontSize = 12.sp)
        Text("Snapchat Version: ${metadata.scVersion}", fontSize = 12.sp)
        Text("Pack Version: ${metadata.packVersion}", fontSize = 12.sp)
    }
}
