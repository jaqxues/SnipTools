package com.jaqxues.sniptools.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.jaqxues.akrolyb.utils.XposedUtils
import com.jaqxues.sniptools.R
import com.jaqxues.sniptools.pack.*
import com.jaqxues.sniptools.ui.screens.*
import com.jaqxues.sniptools.ui.composables.EmptyScreenMessage
import com.jaqxues.sniptools.ui.theme.DarkTheme
import com.jaqxues.sniptools.ui.viewmodel.KnownBugsViewModel
import com.jaqxues.sniptools.ui.viewmodel.PackViewModel
import com.jaqxues.sniptools.ui.viewmodel.ServerPackViewModel
import com.jaqxues.sniptools.ui.viewmodel.SettingsViewModel
import com.jaqxues.sniptools.utils.Either
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 31.08.20 - Time 14:32.
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
fun AppUi() {
    DarkTheme {
        val scaffoldState = rememberScaffoldState()
        val navController = rememberNavController()
        val currentBackStackEntry by navController.currentBackStackEntryAsState()

        val allLocalRoutes = remember { LocalScreen.allScreens.associateBy { it.route } }

        val packViewModel = viewModel<PackViewModel>()
        val loadedPacks = remember { mutableStateMapOf<String, ModPack>() }

        LaunchedEffect(packViewModel) {
            packViewModel.packLoadChanges.collect { (packName, state) ->
                when (state) {
                    is StatefulPackData.LoadedPack -> {
                        loadedPacks[packName] = state.pack
                    }
                    else -> {
                        loadedPacks -= packName
                    }
                }
            }
        }

        val packDestinations = loadedPacks.mapValues { (_, pack) ->
            pack.disabledFeatures.observeAsState().value // Ignore state value to force update
            pack.staticFragments + pack.featureManager.getActiveFeatures().flatMap {
                it.getDestinations().toList()
            }
        }

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                val (pack, currentRoute) = remember(currentBackStackEntry) { currentBackStackEntry.routeInfo }

                val currentScreen = remember(pack, currentRoute) {
                    if (pack == null) {
                        allLocalRoutes[currentRoute]
                    } else {
                        KnownExternalDestinations.byRoute[currentRoute]
                            ?: packDestinations[pack]?.find { it.route == currentRoute }
                    }
                }

                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text("SnipTools")

                                // SubTitle if data is available for current screen
                                currentScreen?.let { screen ->
                                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                        Text(
                                            screen.screenName,
                                            fontWeight = FontWeight.Normal, fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            if (currentScreen?.isTopLevelScreen == true) {
                                val scope = rememberCoroutineScope()
                                IconButton(onClick = {
                                    scope.launch {
                                        scaffoldState.drawerState.open()
                                    }
                                }) {
                                    Icon(Icons.Default.Menu, "Open Menu")
                                }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.Default.ArrowBack, "Go Back")
                                }
                            }
                        }
                    )
                    if (!XposedUtils.isHooked) {
                        Text(
                            text = "Xposed Module not active",
                            color = MaterialTheme.colors.onError,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                                .background(MaterialTheme.colors.error)
                                .padding(8.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            },
            drawerElevation = 2.dp,
            drawerContent = {
                // Stop Drawer from closing when touching on non-clickable elements
                Box(Modifier.fillMaxSize()) {
                    val scope = rememberCoroutineScope()
                    DrawerContent(
                        navController,
                        packDestinations
                    ) {
                        scope.launch {
                            scaffoldState.drawerState.close()
                        }
                    }
                }
            }
        ) {
            Routing(
                navController,
                packViewModel
            ) { packName, packScreen ->
                remember(packName, packScreen) {
                    packDestinations[packName]
                        ?.find { it.route == packScreen }
                        ?.screenComposable
                }?.invoke()
            }
        }
    }
}

data class RouteInfo(val packRoute: String? = null, val screenRoute: String? = null)

val NavBackStackEntry?.routeInfo: RouteInfo
    get() =
        this?.destination?.route?.let {
            if (it.startsWith("pack/")) {
                RouteInfo(arguments?.getString("pack_name"), arguments?.getString("pack_screen"))
            } else {
                RouteInfo(
                    null, it
                        .replaceAfter('/', "").replace("/", "")
                        .replaceAfter('?', "").replace("?", "")
                )
            }
        } ?: RouteInfo()

@Composable
fun Routing(
    navController: NavHostController,
    packViewModel: PackViewModel,
    openPackScreen: @Composable (String, String) -> Unit
) {
    val serverPackViewModel = viewModel<ServerPackViewModel>()
    val knownBugsViewModel = viewModel<KnownBugsViewModel>()
    val settingsViewModel = viewModel<SettingsViewModel>()

    NavHost(navController, LocalScreen.Home.route) {
        composable(LocalScreen.Home.route) { HomeScreen() }
        composable(
            "${LocalScreen.PackManager.route}?tab={tab}?pack_name={pack_name}",
            listOf(
                navArgument("tab") {
                    nullable = true
                },
                navArgument("pack_name") {
                    nullable = true
                }
            )
        ) {
            PackManagerScreen(
                navController,
                packViewModel,
                serverPackViewModel,
                it.arguments?.getString("tab")?.let { PackManagerTabs.valueOf(it) },
                it.arguments?.getString("pack_name")
            )
        }
        composable(LocalScreen.Settings.route) { SettingsScreen(settingsViewModel) }
        composable(LocalScreen.Faqs.route) { EmptyScreenMessage("Screen not available") }
        composable(LocalScreen.Support.route) { SupportScreen() }
        composable(LocalScreen.AboutUs.route) { EmptyScreenMessage("Screen not available") }
        composable(LocalScreen.Shop.route) { EmptyScreenMessage("Screen not available") }
        composable(LocalScreen.Features.route) { EmptyScreenMessage("Screen not available") }
        composable(LocalScreen.Legal.route) { EmptyScreenMessage("Screen not available") }

        composable(
            "${LocalScreen.KnownBugs.route}/{sc_version}/{pack_version}", listOf(
                navArgument("sc_version") {},
                navArgument("pack_version") {}
            )
        ) {
            KnownBugsScreen(
                it.arguments!!.getString("sc_version")!!,
                it.arguments!!.getString("pack_version")!!,
                knownBugsViewModel
            )
        }

        composable(
            "${LocalScreen.PackHistory.route}/{sc_version}",
            listOf(navArgument("sc_version") {})
        ) {
            PackHistoryScreen(
                navController,
                it.arguments!!.getString("sc_version")!!,
                serverPackViewModel
            )
        }
        composable(
            "pack/{pack_name}/{pack_screen}",
            arguments = listOf(
                navArgument("pack_name") {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("pack_screen") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            openPackScreen(
                it.arguments!!.getString("pack_name")!!,
                it.arguments!!.getString("pack_screen")!!
            )
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavController,
    loadedPackDestinations: Map<String, Array<ExternalDestination>>,
    closeDrawer: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        // use `item` for separate elements like headers
        // and `items` for lists of identical elements
        item {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(R.drawable.sniptools_logo),
                    "App Logo",
                    Modifier.padding(8.dp)
                )
                Column(Modifier.padding(16.dp)) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                        Text("SnipTools")
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text("I hope you're happy now", fontSize = 12.sp)
                    }
                }
            }

            HomeDivider()

            val (pack, route) = navController.currentBackStackEntryAsState().value.routeInfo

            LocalScreen.topLevelScreens.forEach {
                val selected = pack == null && it.route == route
                DrawerButton(
                    label = it.screenName,
                    icon = it.icon,
                    isSelected = selected,
                    action = {
                        if (!selected) {
                            navController.popBackStack(navController.graph.startDestinationId, false)
                            navController.navigate(it.route)
                        }
                        closeDrawer()
                    }
                )
            }
            loadedPackDestinations.forEach { (packName, destinations) ->
                Divider(Modifier.padding(horizontal = 16.dp))
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        packName,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 32.dp),
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp
                    )
                }

                for (destination in destinations) {
                    val known = KnownExternalDestinations.byRoute[destination.route]

                    val (icon, label) = if (known == null) {
                        null to destination.defaultName
                    } else {
                        known.icon to stringResource(known.stringRes)
                    }
                    val selected = packName == pack && destination.route == route

                    DrawerButton(
                        icon = icon,
                        label = label,
                        isSelected = selected,
                        action = {
                            if (!selected) {
                                navController.popBackStack(
                                    navController.graph.startDestinationId,
                                    false
                                )
                                navController.navigate("pack/$packName/${destination.route}")
                            }
                            closeDrawer()
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun DrawerButton(
    label: String,
    icon: DestinationIcon? = null,
    isSelected: Boolean,
    action: () -> Unit
) {
    val colors = MaterialTheme.colors

    val (imageAlpha, textIconColor, backgroundColor) =
        if (isSelected) {
            Triple(1f, colors.primary, colors.primary.copy(alpha = 0.12f))
        } else {
            Triple(0.6f, colors.onSurface, Color.Transparent)
        }

    Surface(
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp, end = 8.dp)
            .fillMaxWidth(),
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        TextButton(
            onClick = action,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
            ) {
                if (icon != null)
                    ImageFromEither(
                        either = icon,
                        textIconColor = textIconColor,
                        imageAlpha = imageAlpha
                    )
                else
                    Spacer(Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.body2,
                    color = textIconColor
                )
            }
        }
    }
}

@Composable
fun ImageFromEither(either: DestinationIcon, textIconColor: Color, imageAlpha: Float) {
    when (either) {
        is Either.Left -> {
            Image(
                painter = either.value,
                null,
                colorFilter = ColorFilter.tint(textIconColor),
                alpha = imageAlpha,
                modifier = Modifier.size(24.dp)
            )
        }
        is Either.Right -> {
            Image(
                imageVector = either.value,
                null,
                colorFilter = ColorFilter.tint(textIconColor),
                alpha = imageAlpha,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}