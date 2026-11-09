/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.sheetStateForPreview
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * For parameter [scrollable], set it to true if the content of the sheet does not already contain a scrollable component, such as a LazyColumn,
 * to avoid nested scroll issues. In this case, the content will be wrapped in a Column with verticalScroll.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    scrollable: Boolean,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
    ),
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = if (ElementTheme.isLightTheme) 0.dp else BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.modalWindowInsets },
    content: @Composable ColumnScope.() -> Unit,
) {
    val safeSheetState = if (LocalInspectionMode.current) sheetStateForPreview() else sheetState
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.onKeyEvent { keyEvent ->
            // It seems that on some devices, we have to handle the Escape key manually to close the bottom sheet.
            // This is not the case using an emulator, but is necessary on some physical devices.
            if (keyEvent.type == KeyEventType.KeyUp &&
                keyEvent.key == Key.Escape) {
                onDismissRequest()
                true
            } else {
                false
            }
        },
        sheetState = safeSheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
    ) {
        UnstuckSheetStateEffect(safeSheetState)
        val movableContent = remember { movableContentOf { content() } }
        if (scrollable) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                movableContent()
            }
        } else {
            movableContent()
        }
    }
}

/**
 * Workaround for https://github.com/element-hq/element-x-android/issues/8102.
 *
 * When the content of the sheet is taller than the screen, the sheet height changes while it is being flung to the top
 * (the top window insets start to be applied). The anchors are then updated while the fling holds the drag lock, and the
 * internal `dragTarget` of the `AnchoredDraggableState` is never reset. [SheetState.isAnimationRunning] then stays true
 * forever, and the sheet consumes all the down events (it believes it has to catch an ongoing animation), so nothing
 * inside the sheet can be clicked anymore.
 * Note that the stale target also pins [SheetState.targetValue], so it can differ from [SheetState.currentValue].
 *
 * A legit animation moves the sheet at every frame, so the state is considered stuck when an animation is reported as
 * running but the sheet did not move for a few frames. Animating to the current value then clears the stale state,
 * without moving the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnstuckSheetStateEffect(sheetState: SheetState) {
    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.isAnimationRunning }
            .collectLatest { isAnimationRunning ->
                if (!isAnimationRunning) return@collectLatest
                resetWhenStuck(
                    isAnimationRunning = { sheetState.isAnimationRunning },
                    offset = { tryOrNull { sheetState.requireOffset() } },
                    currentValue = { sheetState.currentValue },
                    targetValue = { sheetState.targetValue },
                    animateTo = { value ->
                        when (value) {
                            SheetValue.Expanded -> sheetState.expand()
                            SheetValue.PartiallyExpanded -> sheetState.partialExpand()
                            SheetValue.Hidden -> Unit
                        }
                    },
                )
            }
    }
}

/**
 * Reset the sheet state, by animating to its current value, each time it is stuck at rest, see [awaitStuckAtRest].
 * Return when no animation is running anymore, or when the sheet is hidden, or after [maxResets] resets which did
 * not clear the stuck state.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal suspend fun resetWhenStuck(
    isAnimationRunning: () -> Boolean,
    offset: () -> Float?,
    currentValue: () -> SheetValue,
    targetValue: () -> SheetValue,
    animateTo: suspend (SheetValue) -> Unit,
    stableFrameCount: Int = STUCK_STABLE_FRAME_COUNT,
    maxResets: Int = MAX_RESET_COUNT,
) {
    var resetCount = 0
    while (awaitStuckAtRest(isAnimationRunning, offset, stableFrameCount)) {
        val value = currentValue()
        if (value == SheetValue.Hidden) return
        // The stale target points at a visible anchor. If the target is Hidden, this is a legit hide animation:
        // never interrupt it, the dismiss callback would be lost.
        if (targetValue() == SheetValue.Hidden) continue
        try {
            animateTo(value)
            resetCount++
            if (!isAnimationRunning()) {
                Timber.w("Sheet state was stuck in $value, it has been reset")
            } else if (resetCount >= maxResets) {
                Timber.e("Sheet state is stuck in $value, and resetting it does not work, giving up")
                return
            }
        } catch (e: CancellationException) {
            // Rethrow if this coroutine is cancelled, else the reset was interrupted by a user drag
            // (which has a higher priority): try again once the sheet is at rest.
            currentCoroutineContext().ensureActive()
        }
    }
}

/**
 * Suspend until either the animation is no longer running, and then return false, or the animation is reported
 * as running but [offset] did not change during [stableFrameCount] consecutive frames, and then return true.
 */
internal suspend fun awaitStuckAtRest(
    isAnimationRunning: () -> Boolean,
    offset: () -> Float?,
    stableFrameCount: Int = STUCK_STABLE_FRAME_COUNT,
): Boolean {
    var lastOffset = offset()
    var stableFrames = 0
    while (isAnimationRunning()) {
        withFrameNanos { }
        val newOffset = offset()
        stableFrames = if (newOffset != null && newOffset == lastOffset) stableFrames + 1 else 0
        lastOffset = newOffset
        if (stableFrames >= stableFrameCount && isAnimationRunning()) {
            return true
        }
    }
    return false
}

// More than one frame, since a restarted animation does not move during its first frame. A bit more to be safe, since
// a false positive would interrupt a legit animation.
private const val STUCK_STABLE_FRAME_COUNT = 6
private const val MAX_RESET_COUNT = 3

@OptIn(ExperimentalMaterial3Api::class)
fun SheetState.hide(coroutineScope: CoroutineScope, then: suspend () -> Unit) {
    coroutineScope.launch {
        hide()
        if (!isVisible) {
            then()
        }
    }
}

@Preview(group = PreviewGroup.BottomSheets)
@Composable
internal fun ModalBottomSheetLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview(group = PreviewGroup.BottomSheets)
@Composable
internal fun ModalBottomSheetDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@OptIn(ExperimentalMaterial3Api::class)
@ExcludeFromCoverage
@Composable
private fun ContentToPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        ModalBottomSheet(
            onDismissRequest = {},
            scrollable = false,
        ) {
            Text(
                text = "Sheet Content",
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
                    .background(color = Color.Green)
            )
        }
    }
}
