/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
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
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import io.element.android.features.location.api.internal.rememberTileStyleUrl
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.BottomSheetScaffold
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.MaplibreComposable
import kotlin.math.roundToInt

/**
 * A reusable scaffold component for map views with a bottom sheet.
 *
 * Handles the layout complexity of:
 * - Calculating the visible sheet height dynamically
 * - Updating camera position padding based on sheet height
 * - Rendering the MaplibreMap with proper ornament positioning
 *
 * @param modifier Modifier for the root layout
 * @param scaffoldState State for the bottom sheet scaffold
 * @param cameraState The camera state for the map
 * @param mapOptions The options to configure the map
 * @param sheetPeekHeight The height of the sheet when collapsed
 * @param sheetDragHandle Optional drag handle for the sheet
 * @param sheetSwipeEnabled Whether the sheet can be swiped
 * @param topBar The top app bar content
 * @param snackbarHost The snackbar host content
 * @param sheetContent The content to display in the bottom sheet
 * @param mapContent The content inside the MaplibreMap (layers, location pucks, etc.)
 * @param overlayContent Content to overlay on top of the map (FAB, pin icons, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBottomSheetScaffold(
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    ),
    cameraState: CameraState = rememberCameraState(),
    mapOptions: MapOptions = MapDefaults.options,
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    sheetDragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    sheetSwipeEnabled: Boolean = true,
    topBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    sheetContent: @Composable ColumnScope.(PaddingValues) -> Unit = {},
    mapContent: @Composable @MaplibreComposable () -> Unit = {},
    overlayContent: @Composable BoxScope.(sheetPadding: PaddingValues) -> Unit = {},
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
        // Update camera position when sheet padding changes
        LaunchedEffect(sheetPadding) {
            cameraState.position = cameraState.position.copy(padding = sheetPadding)
        }
        BottomSheetScaffold(
            modifier = Modifier,
            sheetPeekHeight = sheetPeekHeight,
            sheetContent = {
                val maxContentHeight = (layoutHeightPx * 0.5f).roundToInt().toDp()
                Column(modifier = Modifier.heightIn(max = maxContentHeight)) {
                    sheetContent(sheetPadding)
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            },
            scaffoldState = scaffoldState,
            sheetDragHandle = sheetDragHandle,
            sheetSwipeEnabled = sheetSwipeEnabled,
            snackbarHost = snackbarHost,
            topBar = topBar,
        ) {
            val ornamentOptions = mapOptions.ornamentOptions.copy(padding = sheetPadding)
            val mapOptions = mapOptions.copy(ornamentOptions = ornamentOptions)
            Box {
                MaplibreMap(
                    options = mapOptions,
                    baseStyle = BaseStyle.Uri(rememberTileStyleUrl()),
                    modifier = Modifier.fillMaxSize(),
                    cameraState = cameraState,
                    content = mapContent,
                )
                overlayContent(sheetPadding)
            }
        }
    }
}
