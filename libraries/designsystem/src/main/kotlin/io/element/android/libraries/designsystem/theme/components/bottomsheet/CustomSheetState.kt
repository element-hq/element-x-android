/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components.bottomsheet

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalFoundationApi::class)
@Stable
@ExperimentalMaterial3Api
class CustomSheetState
@Deprecated(
    message = "This constructor is deprecated. " +
        "Please use the constructor that provides a [Density]",
    replaceWith = ReplaceWith(
        "SheetState(" +
            "skipPartiallyExpanded, LocalDensity.current, initialValue, " +
            "confirmValueChange, skipHiddenState)"
    )
)
constructor(
    internal val skipPartiallyExpanded: Boolean,
    initialValue: SheetValue = Hidden,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    internal val skipHiddenState: Boolean = false,
) {
    /**
     * State of a sheet composable, such as [ModalBottomSheet]
     *
     * Contains states relating to its swipe position as well as animations between state values.
     *
     * @param skipPartiallyExpanded Whether the partially expanded state, if the sheet is large
     * enough, should be skipped. If true, the sheet will always expand to the [Expanded] state and move
     * to the [Hidden] state if available when hiding the sheet, either programmatically or by user
     * interaction.
     * @param density The density that this state can use to convert values to and from dp.
     * @param initialValue The initial value of the state.
     * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
     * @param skipHiddenState Whether the hidden state should be skipped. If true, the sheet will always
     * expand to the [Expanded] state and move to the [PartiallyExpanded] if available, either
     * programmatically or by user interaction.
     */
    @ExperimentalMaterial3Api
    @Suppress("Deprecation")
    constructor(
        skipPartiallyExpanded: Boolean,
        density: Density,
        initialValue: SheetValue = Hidden,
        confirmValueChange: (SheetValue) -> Boolean = { true },
        skipHiddenState: Boolean = false,
    ) : this(skipPartiallyExpanded, initialValue, confirmValueChange, skipHiddenState) {
        this.density = density
    }

    init {
        if (skipPartiallyExpanded) {
            require(initialValue != PartiallyExpanded) {
                "The initial value must not be set to PartiallyExpanded if skipPartiallyExpanded " +
                    "is set to true."
            }
        }
        if (skipHiddenState) {
            require(initialValue != Hidden) {
                "The initial value must not be set to Hidden if skipHiddenState is set to true."
            }
        }
    }

    /**
     * The current value of the state.
     *
     * If no swipe or animation is in progress, this corresponds to the state the bottom sheet is
     * currently in. If a swipe or an animation is in progress, this corresponds the state the sheet
     * was in before the swipe or animation started.
     */

    val currentValue: SheetValue get() = anchoredDraggableState.currentValue

    /**
     * The target value of the bottom sheet state.
     *
     * If a swipe is in progress, this is the value that the sheet would animate to if the
     * swipe finishes. If an animation is running, this is the target value of that animation.
     * Finally, if no swipe or animation is in progress, this is the same as the [currentValue].
     */
    val targetValue: SheetValue get() = anchoredDraggableState.targetValue

    /**
     * Whether the modal bottom sheet is visible.
     */
    val isVisible: Boolean
        get() = anchoredDraggableState.currentValue != Hidden

    /**
     * Require the current offset (in pixels) of the bottom sheet.
     *
     * The offset will be initialized during the first measurement phase of the provided sheet
     * content.
     *
     * These are the phases:
     * Composition { -> Effects } -> Layout { Measurement -> Placement } -> Drawing
     *
     * During the first composition, an [IllegalStateException] is thrown. In subsequent
     * compositions, the offset will be derived from the anchors of the previous pass. Always prefer
     * accessing the offset from a LaunchedEffect as it will be scheduled to be executed the next
     * frame, after layout.
     *
     * @throws IllegalStateException If the offset has not been initialized yet
     */
    fun requireOffset(): Float = anchoredDraggableState.requireOffset()

    fun getOffset(): Float? = anchoredDraggableState.offset.takeIf { !it.isNaN() }

    /**
     * Whether the sheet has an expanded state defined.
     */

    val hasExpandedState: Boolean
        get() = anchoredDraggableState.anchors.hasAnchorFor(Expanded)

    /**
     * Whether the modal bottom sheet has a partially expanded state defined.
     */
    val hasPartiallyExpandedState: Boolean
        get() = anchoredDraggableState.anchors.hasAnchorFor(PartiallyExpanded)

    /**
     * Fully expand the bottom sheet with animation and suspend until it is fully expanded or
     * animation has been cancelled.
     * *
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun expand() {
        anchoredDraggableState.animateTo(Expanded)
    }

    /**
     * Animate the bottom sheet and suspend until it is partially expanded or animation has been
     * cancelled.
     * @throws [CancellationException] if the animation is interrupted
     * @throws [IllegalStateException] if [skipPartiallyExpanded] is set to true
     */
    suspend fun partialExpand() {
        check(!skipPartiallyExpanded) {
            "Attempted to animate to partial expanded when skipPartiallyExpanded was enabled. Set" +
                " skipPartiallyExpanded to false to use this function."
        }
        animateTo(PartiallyExpanded)
    }

    /**
     * Expand the bottom sheet with animation and suspend until it is [PartiallyExpanded] if defined
     * else [Expanded].
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun show() {
        val targetValue = when {
            hasPartiallyExpandedState -> PartiallyExpanded
            else -> Expanded
        }
        animateTo(targetValue)
    }

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled.
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun hide() {
        check(!skipHiddenState) {
            "Attempted to animate to hidden when skipHiddenState was enabled. Set skipHiddenState" +
                " to false to use this function."
        }
        animateTo(Hidden)
    }

    /**
     * Animate to a [targetValue].
     * If the [targetValue] is not in the set of anchors, the [currentValue] will be updated to the
     * [targetValue] without updating the offset.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     */
    @OptIn(ExperimentalFoundationApi::class)
    internal suspend fun animateTo(
        targetValue: SheetValue,
    ) {
        anchoredDraggableState.animateTo(targetValue)
    }

    /**
     * Snap to a [targetValue] without any animation.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     */
    @OptIn(ExperimentalFoundationApi::class)
    internal suspend fun snapTo(targetValue: SheetValue) {
        anchoredDraggableState.snapTo(targetValue)
    }

    /**
     * Find the closest anchor taking into account the velocity and settle at it with an animation.
     */
    @OptIn(ExperimentalFoundationApi::class)
    internal suspend fun settle(velocity: Float) {
        anchoredDraggableState.settle(velocity)
    }

    @OptIn(ExperimentalFoundationApi::class)
    internal var anchoredDraggableState = androidx.compose.foundation.gestures.AnchoredDraggableState(
        initialValue = initialValue,
        snapAnimationSpec = AnchoredDraggableDefaults.SnapAnimationSpec,
        decayAnimationSpec = AnchoredDraggableDefaults.DecayAnimationSpec,
        confirmValueChange = confirmValueChange,
        positionalThreshold = { with(requireDensity()) { 56.dp.toPx() } },
        velocityThreshold = { with(requireDensity()) { 125.dp.toPx() } }
    )

    @OptIn(ExperimentalFoundationApi::class)
    internal val offset: Float? get() = anchoredDraggableState.offset

    internal var density: Density? = null
    private fun requireDensity() = requireNotNull(density) {
        "SheetState did not have a density attached. Are you using SheetState with " +
            "BottomSheetScaffold or ModalBottomSheet component?"
    }

    companion object {
        /**
         * The default [Saver] implementation for [SheetState].
         */
        @SuppressWarnings("FunctionName")
        fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (SheetValue) -> Boolean,
            density: Density
        ) = Saver<CustomSheetState, SheetValue>(
            save = { it.currentValue },
            restore = { savedValue ->
                CustomSheetState(skipPartiallyExpanded, density, savedValue, confirmValueChange)
            }
        )

        /**
         * The default [Saver] implementation for [SheetState].
         */
        @Deprecated(
            message = "This function is deprecated. Please use the overload where Density is" +
                " provided.",
            replaceWith = ReplaceWith(
                "Saver(skipPartiallyExpanded, confirmValueChange, LocalDensity.current)"
            )
        )
        @Suppress("Deprecation", "FunctionName")
        fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (SheetValue) -> Boolean
        ) = Saver<CustomSheetState, SheetValue>(
            save = { it.currentValue },
            restore = { savedValue ->
                CustomSheetState(skipPartiallyExpanded, savedValue, confirmValueChange)
            }
        )
    }
}

@Stable
@ExperimentalMaterial3Api
internal object AnchoredDraggableDefaults {
    /**
     * The default animation used by [AnchoredDraggableState].
     */
    @ExperimentalMaterial3Api
    val SnapAnimationSpec = SpringSpec<Float>()

    @ExperimentalMaterial3Api
    val DecayAnimationSpec = exponentialDecay<Float>()
}
