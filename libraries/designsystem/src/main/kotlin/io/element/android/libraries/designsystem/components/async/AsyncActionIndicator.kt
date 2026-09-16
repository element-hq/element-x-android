/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.async

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.element.android.libraries.architecture.AsyncAction
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val DefaultShowDelay = 300.milliseconds
private val DefaultMinDisplayDuration = 500.milliseconds
private val DefaultFailureDuration = 500.milliseconds

/**
 * An [AsyncIndicatorHost] driven by an [AsyncAction], with anti-flicker timing.
 *
 * While [asyncAction] is [AsyncAction.Loading], [loading] is displayed, but only if the operation
 * runs longer than [showDelay]; once shown it stays visible for at least [minDisplayDuration]. This
 * prevents the indicator from flashing for operations that complete almost instantly.
 *
 * When [asyncAction] is [AsyncAction.Failure], [failure] is displayed immediately (failures are
 * never debounced) and auto-dismisses after [failureDuration].
 *
 * @param asyncAction the action whose progress drives the indicator.
 * @param loading the composable to display while loading.
 * @param failure the composable to display on failure.
 * @param modifier the modifier to apply to the underlying [AsyncIndicatorHost].
 * @param state the [AsyncIndicatorState] backing the host.
 * @param showDelay how long the loading state must persist before the indicator is shown.
 * @param minDisplayDuration the minimum duration the loading indicator stays visible once shown.
 * @param failureDuration how long the failure indicator stays visible before auto-dismissing.
 */
@Composable
fun AsyncActionIndicator(
    asyncAction: AsyncAction<*>,
    loading: @Composable () -> Unit,
    failure: @Composable (Throwable) -> Unit,
    modifier: Modifier = Modifier,
    state: AsyncIndicatorState = rememberAsyncIndicatorState(),
    showDelay: Duration = DefaultShowDelay,
    minDisplayDuration: Duration = DefaultMinDisplayDuration,
    failureDuration: Duration = DefaultFailureDuration,
) {
    AsyncIndicatorHost(modifier = modifier, state = state)
    AsyncActionIndicatorEffect(
        asyncAction = asyncAction,
        state = state,
        showDelay = showDelay,
        minDisplayDuration = minDisplayDuration,
        failureDuration = failureDuration,
        loading = loading,
        failure = failure,
    )
}

@Composable
internal fun AsyncActionIndicatorEffect(
    asyncAction: AsyncAction<*>,
    state: AsyncIndicatorState,
    showDelay: Duration,
    minDisplayDuration: Duration,
    failureDuration: Duration,
    loading: @Composable () -> Unit,
    failure: @Composable (Throwable) -> Unit,
    timeSource: TimeSource = TimeSource.Monotonic,
) {
    // Referenced inside the restarting LaunchedEffect below, so keep them up to date.
    val currentLoading by rememberUpdatedState(loading)
    val currentFailure by rememberUpdatedState(failure)
    // Mark set when the loader actually becomes visible, null while it is hidden.
    var loaderShownAt by remember { mutableStateOf<TimeMark?>(null) }
    LaunchedEffect(asyncAction) {
        when (asyncAction) {
            is AsyncAction.Loading -> {
                // If the operation completes before this delay elapses, the effect is cancelled
                // and the loader is never shown.
                delay(showDelay)
                state.enqueue(composable = currentLoading)
                loaderShownAt = timeSource.markNow()
            }
            is AsyncAction.Failure -> {
                // Always surface failures immediately, even if the loader was never shown.
                val error = asyncAction.error
                state.enqueue(durationMs = failureDuration.inWholeMilliseconds, composable = { currentFailure(error) })
                loaderShownAt = null
            }
            is AsyncAction.Success -> {
                // Hold the loader for its minimum duration before clearing it.
                loaderShownAt?.let { shownAt ->
                    val remaining = minDisplayDuration - shownAt.elapsedNow()
                    if (remaining > Duration.ZERO) delay(remaining)
                }
                state.clear()
                loaderShownAt = null
            }
            else -> Unit
        }
    }
}
