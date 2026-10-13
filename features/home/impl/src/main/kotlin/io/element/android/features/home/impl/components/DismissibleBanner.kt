/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.first

/**
 * Wraps a banner of the room list so that it shrinks and fades out when it is dismissed.
 * [onDismiss] is invoked once the animation is over, so that the banner is removed from the list
 * when its height is 0, and the items below it are not moved abruptly.
 *
 * @param onDismiss invoked when the banner has been dismissed and the animation is over.
 * @param modifier the modifier to apply to the banner container.
 * @param content the banner, which must invoke the provided lambda when the user dismisses it.
 */
@Composable
internal fun DismissibleBanner(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (onDismissClick: () -> Unit) -> Unit,
) {
    val visibleState = remember { MutableTransitionState(true) }
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    var isDismissNotified by remember { mutableStateOf(false) }
    fun notifyDismiss() {
        if (!isDismissNotified) {
            isDismissNotified = true
            latestOnDismiss()
        }
    }
    LaunchedEffect(visibleState) {
        snapshotFlow { visibleState.isIdle && !visibleState.currentState }.first { it }
        notifyDismiss()
    }
    DisposableEffect(visibleState) {
        onDispose {
            // The item can be disposed during the animation, for instance if the user scrolls the list
            if (!visibleState.targetState) {
                notifyDismiss()
            }
        }
    }
    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = EnterTransition.None,
        exit = shrinkVertically(
            animationSpec = tween(),
            shrinkTowards = Alignment.CenterVertically,
        ) + fadeOut(
            animationSpec = tween(),
        ),
    ) {
        content { visibleState.targetState = false }
    }
}
