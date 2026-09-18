/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.BottomSheetScaffold
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MapUiOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.RenderOptions
import org.maplibre.compose.overlay.ExpandingAttributionButton
import org.maplibre.compose.overlay.MaplibreLogo
import kotlin.math.roundToInt

/**
 * A reusable scaffold component for map views with a bottom sheet.
 *
 * Handles the layout complexity of:
 * - Calculating the visible sheet height dynamically
 * - Updating camera position padding based on sheet height
 * - Rendering the MaplibreMap with proper ornament positioning
 *
 * @param isReady Used to gate rendering until the map should be displayed
 * @param modifier Modifier for the root layout
 * @param scaffoldState State for the bottom sheet scaffold
 * @param mapState The map state (camera, style and layer content) for the map, created with
 *   [rememberLocationMapState]. `null` in inspection/preview mode, where a placeholder is shown.
 * @param renderOptions The options to configure the map rendering
 * @param sheetPeekHeight The height of the sheet when collapsed
 * @param sheetDragHandle Optional drag handle for the sheet
 * @param sheetSwipeEnabled Whether the sheet can be swiped
 * @param topBar The top app bar content
 * @param snackbarHost The snackbar host content
 * @param sheetContent The content to display in the bottom sheet
 * @param overlay Content to overlay on top of the map (FAB, pin icons, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBottomSheetScaffold(
    mapState: MapState?,
    isReady: Boolean,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
        )
    ),
    renderOptions: RenderOptions = RenderOptions.Standard,
    interactions: MapInteractions = MapInteractions.Standard,
    uiOptions: MapUiOptions = MapUiOptions.Standard,
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    sheetDragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    sheetSwipeEnabled: Boolean = true,
    topBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    sheetContent: @Composable ColumnScope.() -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val density = LocalDensity.current
    val windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
    BoxWithConstraints(modifier = modifier.windowInsetsPadding(windowInsets)) {
        val layoutHeightPx by rememberUpdatedState(constraints.maxHeight)
        val sheetPadding by remember {
            derivedStateOf {
                val sheetOffset = tryOrNull { scaffoldState.bottomSheetState.requireOffset() } ?: 0f
                val sheetVisibleHeightPx = layoutHeightPx - sheetOffset
                val bottomPadding = with(density) { max(sheetVisibleHeightPx.roundToInt().toDp(), 0.dp) }
                PaddingValues(bottom = bottomPadding)
            }
        }
        BottomSheetScaffold(
            modifier = Modifier,
            sheetPeekHeight = sheetPeekHeight,
            sheetContent = {
                val maxContentHeight = (layoutHeightPx * 0.5f).roundToInt().toDp()
                Column(modifier = Modifier.heightIn(max = maxContentHeight)) {
                    sheetContent()
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            },
            scaffoldState = scaffoldState,
            sheetDragHandle = sheetDragHandle,
            sheetSwipeEnabled = sheetSwipeEnabled,
            snackbarHost = snackbarHost,
            topBar = topBar,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isReady && mapState != null -> {
                        MaplibreMap(
                            state = mapState,
                            cameraPadding = sheetPadding,
                            renderOptions = renderOptions,
                            interactions = interactions,
                            uiOptions = uiOptions,
                            overlay = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(sheetPadding)
                                ) {
                                    Row(
                                        Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth()
                                            .padding(all = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        MaplibreLogo()
                                        ExpandingAttributionButton()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    mapState == null -> {
                        // Inspection/preview mode: the map cannot initialize, render a placeholder
                        // matching MaplibreMap's own inspection rendering.
                        Box(modifier = Modifier.fillMaxSize().background(Color.Gray))
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
                // App overlay (FABs, pins, live-location indicators). Layered above the map so it is
                // present even in inspection/preview mode, where the map is a placeholder.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(sheetPadding)
                ) {
                    overlay()
                }
            }
        }
    }
}
