/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.messages.impl

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlin.math.roundToInt

/**
 * A [BottomSheetScaffold] that allows the sheet to be expanded the screen height
 * of the sheet contents.
 *
 * @param content The main content.
 * @param sheetContent The sheet content.
 * @param sheetDragHandle The drag handle for the sheet.
 * @param sheetSwipeEnabled Whether the sheet can be swiped.
 * @param sheetShape The shape of the sheet.
 * @param sheetTonalElevation The tonal elevation of the sheet.
 * @param sheetShadowElevation The shadow elevation of the sheet.
 * @param modifier The modifier for the layout.
 * @param sheetContentKey The key for the sheet content. If the key changes, the sheet will be remeasured.
 */
@Composable
internal fun ExpandableBottomSheetScaffold(
    content: @Composable (padding: PaddingValues) -> Unit,
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
    LaunchedEffect(sheetSwipeEnabled) {
        if (!sheetSwipeEnabled) {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }
    val scaffold: @Composable (sheetContent: @Composable () -> Unit, dragHandle: @Composable () -> Unit, peekHeight: Dp) -> Unit =
        { sheetContent, dragHandle, peekHeight ->
            BottomSheetScaffold(
                modifier = Modifier,
                scaffoldState = scaffoldState,
                sheetPeekHeight = peekHeight,
                sheetSwipeEnabled = sheetSwipeEnabled,
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
            val sheetContentSub = subcompose(Slot.SheetContent(sheetContentKey)) { sheetContent(subcomposing = true) }.map {
                it.measure(Constraints(maxWidth = constraints.maxWidth))
            }.first()
            val dragHandleSub = subcompose(Slot.DragHandle) { sheetDragHandle() }.map {
                it.measure(Constraints(maxWidth = constraints.maxWidth))
            }.firstOrNull()
            val dragHandleHeight = dragHandleSub?.height?.toDp() ?: 0.dp

            val peekHeight = min(
                constraints.maxHeight.toDp(), // prevent the sheet from expanding beyond the screen
                sheetContentSub.height.toDp() + dragHandleHeight
            )

            val scaffoldPlaceables = subcompose(Slot.Scaffold) {
                scaffold({
                    Layout(
                        modifier = Modifier.fillMaxHeight(),
                        measurePolicy = { measurables, constraints ->
                            val maxHeight = constraints.maxHeight
                            val offset = scaffoldState.bottomSheetState.getOffset() ?: 0
                            val height = Integer.max(0, maxHeight - offset)
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
                        content = { sheetContent(subcomposing = false) })
                }, sheetDragHandle, peekHeight)
            }.map { measurable: Measurable ->
                measurable.measure(constraints)
            }
            val scaffoldPlaceable = scaffoldPlaceables.first()
            layout(constraints.maxWidth, constraints.maxHeight) {
                scaffoldPlaceable.place(0, 0)
            }
        })
}

private fun SheetState.getOffset(): Int? = try {
    requireOffset().roundToInt()
} catch (e: IllegalStateException) {
    null
}

private sealed class Slot {
    data class SheetContent(val key: Int?): Slot()
    data object DragHandle: Slot()
    data object Scaffold: Slot()
}

