/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Stable
class ExpandableBottomSheetState {
    internal var internalPosition: Position by mutableStateOf(Position.COLLAPSED)
    internal var internalDraggingPercentage: Float by mutableFloatStateOf(
        if (internalPosition == Position.EXPANDED) 1f else 0f
    )

    val position = internalPosition
    val draggingPercentage = internalDraggingPercentage

    enum class Position {
        COLLAPSED,
        DRAGGING,
        EXPANDED
    }
}

@Composable
fun rememberExpandableBottomSheetState(): ExpandableBottomSheetState {
    return remember { ExpandableBottomSheetState() }
}

@Composable
fun ExpandableBottomSheetLayout(
    content: @Composable () -> Unit,
    sheetDragHandle: @Composable BoxScope.() -> Unit,
    bottomSheetContent: @Composable ColumnScope.() -> Unit,
    state: ExpandableBottomSheetState,
    sheetShape: Shape = RectangleShape,
    maxBottomSheetContentHeight: Dp,
    isSwipeGestureEnabled: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
) {
    var minBottomContentHeightPx by remember { mutableIntStateOf(0) }
    var currentBottomContentHeightPx by remember { mutableIntStateOf(minBottomContentHeightPx) }
    val maxBottomContentHeightPx = with(LocalDensity.current) { maxBottomSheetContentHeight.roundToPx() }
    var calculatedMaxBottomContentHeightPx by remember(maxBottomContentHeightPx) { mutableIntStateOf(maxBottomContentHeightPx) }
    val animatable = remember { Animatable(0f) }

    fun calculatePercentage(currentPos: Int, minPos: Int, maxPos: Int): Float {
        val currentProgress = currentPos - minPos
        if (currentProgress < 0) {
            Timber.e("Invalid current progress: $currentProgress, minPos: $minPos, maxPos: $maxPos")
            return 0f
        }
        val total = (maxPos - minPos).toFloat()
        if (total <= 0) {
            Timber.e("Invalid total space: $total, minPos: $minPos, maxPos: $maxPos")
            return 0f
        }
        return currentProgress / total
    }

    LaunchedEffect(animatable.value) {
        if (animatable.isRunning && animatable.value != animatable.targetValue) {
            currentBottomContentHeightPx = animatable.value.roundToInt()
            state.internalDraggingPercentage = calculatePercentage(
                currentPos = currentBottomContentHeightPx,
                minPos = minBottomContentHeightPx,
                maxPos = calculatedMaxBottomContentHeightPx,
            )
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val composables = @Composable {
        content()
        Column(
            modifier = Modifier
                .clip(sheetShape)
                .background(backgroundColor)
                .run {
                    if (isSwipeGestureEnabled) {
                        pointerInput(maxBottomSheetContentHeight) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount ->
                                    val calculatedHeight = max(minBottomContentHeightPx, currentBottomContentHeightPx - dragAmount.roundToInt())
                                    val newHeight = min(calculatedMaxBottomContentHeightPx, calculatedHeight)
                                    state.internalPosition = when (newHeight) {
                                        calculatedMaxBottomContentHeightPx -> ExpandableBottomSheetState.Position.EXPANDED
                                        minBottomContentHeightPx -> ExpandableBottomSheetState.Position.COLLAPSED
                                        else -> ExpandableBottomSheetState.Position.DRAGGING
                                    }
                                    state.internalDraggingPercentage = calculatePercentage(
                                        currentPos = newHeight,
                                        minPos = minBottomContentHeightPx,
                                        maxPos = calculatedMaxBottomContentHeightPx,
                                    )
                                    currentBottomContentHeightPx = newHeight
                                },
                                onDragEnd = {
                                    coroutineScope.launch {
                                        val middle = (calculatedMaxBottomContentHeightPx + minBottomContentHeightPx) / 2
                                        animatable.snapTo(currentBottomContentHeightPx.toFloat())

                                        val destination = if (currentBottomContentHeightPx > middle) {
                                            state.internalPosition = ExpandableBottomSheetState.Position.EXPANDED
                                            calculatedMaxBottomContentHeightPx
                                        } else {
                                            state.internalPosition = ExpandableBottomSheetState.Position.COLLAPSED
                                            minBottomContentHeightPx
                                        }.toFloat()

                                        animatable.animateTo(destination)
                                    }
                                }
                            )
                        }
                    } else {
                        this
                    }
                }
        ) {
            Box(Modifier.fillMaxWidth()) {
                sheetDragHandle()
            }
            bottomSheetContent()
        }
    }
    Layout(
        content = composables,
        modifier = modifier,
        measurePolicy = { measurables, constraints ->
            calculatedMaxBottomContentHeightPx = min(constraints.maxHeight, maxBottomContentHeightPx)

            val contentMeasurables = measurables[0]
            val bottomContentMeasurables = measurables[1]

            val minIntrinsicHeight = bottomContentMeasurables.minIntrinsicHeight(constraints.maxWidth)
            val lastMinBottomContentHeightPx = minBottomContentHeightPx
            minBottomContentHeightPx = min(minIntrinsicHeight, calculatedMaxBottomContentHeightPx)

            val isExpanded = state.position == ExpandableBottomSheetState.Position.EXPANDED
            if (lastMinBottomContentHeightPx != minBottomContentHeightPx && !isExpanded) {
                currentBottomContentHeightPx = minBottomContentHeightPx
            }

            val measuredBottomContent = bottomContentMeasurables.measure(
                Constraints.fixed(
                    constraints.maxWidth,
                    max(minBottomContentHeightPx, currentBottomContentHeightPx)
                )
            )

            var remainingHeight = constraints.maxHeight - currentBottomContentHeightPx
            if (remainingHeight < 0) {
                Timber.e("Remaining height is negative: $remainingHeight, resetting to 0")
                remainingHeight = 0
            }

            val contentPlaceable = contentMeasurables.measure(
                Constraints.fixed(constraints.maxWidth, remainingHeight)
            )

            layout(constraints.maxWidth, constraints.maxHeight) {
                contentPlaceable.place(0, 0)
                measuredBottomContent.place(IntOffset(0, constraints.maxHeight - currentBottomContentHeightPx), zIndex = 10f)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
internal fun GrowingBottomContentScaffoldPreview() {
    LocalView.current.isNestedScrollingEnabled = true
    ExpandableBottomSheetLayout(
        sheetDragHandle = {
            Box(
                modifier =
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .align(Alignment.Center)
                    .size(100.dp, 20.dp)
                    .background(Color.Gray)
            )
        },
        content = {
            Box(Modifier.fillMaxWidth()) {
                Text("This is the main content", modifier = Modifier.padding(16.dp).align(Alignment.Center))
            }
        },
        bottomSheetContent = {
            Box(
                modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Blue)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Green),
                    factory = { context ->
                        CustomEditText(context).apply {
                            val initialText = "1111\n2222\n3333\n4444\n5555\n6666"
                            setText(initialText)
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                        }
                    }
                )
            }
            Text("Heya", modifier = Modifier.padding(vertical = 6.dp))
        },
        maxBottomSheetContentHeight = 1800.dp,
        isSwipeGestureEnabled = true,
        backgroundColor = Color.White,
        state = rememberExpandableBottomSheetState(),
        sheetShape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp),
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.ime)
            .fillMaxSize()
            .background(Color.Red),
    )
}

class CustomEditText(context: Context) : EditText(context) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        parent?.requestDisallowInterceptTouchEvent(true)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        parent?.requestDisallowInterceptTouchEvent(true)
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(event)
    }
}
