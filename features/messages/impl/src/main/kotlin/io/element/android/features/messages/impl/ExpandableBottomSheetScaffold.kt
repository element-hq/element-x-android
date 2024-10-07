/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.messages.impl

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import io.element.android.libraries.designsystem.theme.components.BottomSheetScaffold
import io.element.android.libraries.designsystem.theme.components.bottomsheet.CustomSheetState
import io.element.android.libraries.designsystem.theme.components.bottomsheet.rememberBottomSheetScaffoldState
import io.element.android.libraries.designsystem.theme.components.bottomsheet.rememberStandardBottomSheetState
import kotlin.math.roundToInt

/**
 * A [BottomSheetScaffold] that allows the sheet to be expanded the screen height
 * of the sheet contents.
 *
 * @param content The main content.
 * @param sheetContent The sheet content.
 * @param sheetDragHandle The drag handle for the sheet.
 * @param sheetSwipeEnabled Whether the sheet can be swiped. This value is ignored and swipe is disabled if the sheet content overflows.
 * @param sheetShape The shape of the sheet.
 * @param sheetTonalElevation The tonal elevation of the sheet.
 * @param sheetShadowElevation The shadow elevation of the sheet.
 * @param modifier The modifier for the layout.
 * @param sheetContentKey The key for the sheet content. If the key changes, the sheet will be remeasured.
 */
@Suppress(
    "ContentTrailingLambda",
    // False positive
    "MultipleEmitters",
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpandableBottomSheetScaffold(
    content: @Composable (padding: PaddingValues) -> Unit,
    // False positive, it's not being reused
    @Suppress("ContentSlotReused")
    sheetContent: @Composable (subcomposing: Boolean) -> Unit,
    sheetDragHandle: @Composable () -> Unit,
    sheetSwipeEnabled: Boolean,
    sheetShape: Shape,
    sheetTonalElevation: Dp,
    sheetShadowElevation: Dp,
    modifier: Modifier = Modifier,
    sheetContentKey: Int? = null,
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        )
    )

    // If the content overflows, we disable swipe to prevent the sheet from intercepting
    // scroll events of the sheet content.
    var contentOverflows by remember { mutableStateOf(false) }
    val sheetSwipeEnabledIfPossible by remember(contentOverflows, sheetSwipeEnabled) {
        derivedStateOf {
            sheetSwipeEnabled && !contentOverflows
        }
    }

    LaunchedEffect(sheetSwipeEnabledIfPossible) {
        if (!sheetSwipeEnabledIfPossible) {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    @Composable
    fun Scaffold(
        sheetContent: @Composable () -> Unit,
        dragHandle: @Composable () -> Unit,
        peekHeight: Dp,
    ) {
        BottomSheetScaffold(
            modifier = Modifier,
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetSwipeEnabled = sheetSwipeEnabledIfPossible,
            sheetDragHandle = dragHandle,
            sheetShape = sheetShape,
            content = content,
            sheetContent = { sheetContent() },
            sheetTonalElevation = sheetTonalElevation,
            sheetShadowElevation = sheetShadowElevation,
        )
    }

    SubcomposeLayout(
        modifier = modifier,
        measurePolicy = { constraints: Constraints ->
            val sheetContentSub = subcompose(Slot.SheetContent(sheetContentKey)) { sheetContent(true) }.map {
                it.measure(Constraints(maxWidth = constraints.maxWidth))
            }.first()
            val dragHandleSub = subcompose(Slot.DragHandle) { sheetDragHandle() }.map {
                it.measure(Constraints(maxWidth = constraints.maxWidth))
            }.firstOrNull()
            val dragHandleHeight = dragHandleSub?.height?.toDp() ?: 0.dp

            val maxHeight = constraints.maxHeight.toDp()
            val contentHeight = sheetContentSub.height.toDp() + dragHandleHeight

            contentOverflows = contentHeight > maxHeight

            val peekHeight = min(
                // prevent the sheet from expanding beyond the screen
                maxHeight,
                contentHeight
            )

            val scaffoldPlaceables = subcompose(Slot.Scaffold) {
                Scaffold({
                    Layout(
                        modifier = Modifier.fillMaxHeight(),
                        measurePolicy = { measurables, constraints ->
                            val constraintHeight = constraints.maxHeight
                            val offset = scaffoldState.bottomSheetState.getIntOffset() ?: 0
                            val height = Integer.max(0, constraintHeight - offset)
                            val top = measurables[0].measure(
                                constraints.copy(
                                    minHeight = height,
                                    maxHeight = height
                                )
                            )
                            layout(constraints.maxWidth, constraints.maxHeight) {
                                top.place(x = 0, y = 0)
                            }
                        },
                        content = { sheetContent(false) }
                    )
                }, sheetDragHandle, peekHeight)
            }.map { measurable: Measurable ->
                measurable.measure(constraints)
            }
            val scaffoldPlaceable = scaffoldPlaceables.first()
            layout(constraints.maxWidth, constraints.maxHeight) {
                scaffoldPlaceable.place(0, 0)
            }
        }
    )
}

private fun CustomSheetState.getIntOffset(): Int? = try {
    requireOffset().roundToInt()
} catch (e: IllegalStateException) {
    null
}

private sealed interface Slot {
    data class SheetContent(val key: Int?) : Slot
    data object DragHandle : Slot
    data object Scaffold : Slot
}
