/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.theme.components.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex

/**
 * A global dispatcher of [SnackbarMessage] to be displayed in [Snackbar] via a [SnackbarHostState].
 */
class SnackbarDispatcher {
    private val queueMutex = Mutex()
    private val snackBarMessageQueue = ArrayDeque<SnackbarMessage>()
    val snackbarMessage: Flow<SnackbarMessage?> = flow {
        while (currentCoroutineContext().isActive) {
            queueMutex.lock()
            emit(snackBarMessageQueue.firstOrNull())
        }
    }

    fun post(message: SnackbarMessage) {
        if (snackBarMessageQueue.isEmpty()) {
            snackBarMessageQueue.add(message)
            if (queueMutex.isLocked) queueMutex.unlock()
        } else {
            snackBarMessageQueue.add(message)
        }
    }

    fun clear() {
        if (snackBarMessageQueue.isNotEmpty()) {
            snackBarMessageQueue.removeFirstOrNull()
            if (queueMutex.isLocked) queueMutex.unlock()
        }
    }
}

/** Used to provide a [SnackbarDispatcher] to composable functions, it's needed for [rememberSnackbarHostState]. */
val LocalSnackbarDispatcher = compositionLocalOf { SnackbarDispatcher() }

@Composable
fun SnackbarDispatcher.collectSnackbarMessageAsState(): State<SnackbarMessage?> {
    return snackbarMessage.collectAsState(initial = null)
}

/**
 * Helper method to display a [SnackbarMessage] in a [SnackbarHostState] handling cancellations.
 */
@Composable
fun rememberSnackbarHostState(snackbarMessage: SnackbarMessage?): SnackbarHostState {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessageText = snackbarMessage?.let {
        stringResource(id = snackbarMessage.messageResId)
    } ?: return snackbarHostState

    val dispatcher = LocalSnackbarDispatcher.current
    LaunchedEffect(snackbarMessage.id) {
        // If the message wasn't already displayed, do it now, and mark it as displayed
        // This will prevent the message from appearing in any other active SnackbarHosts
        if (snackbarMessage.isDisplayed.getAndSet(true).not()) {
            try {
                snackbarHostState.showSnackbar(
                    message = snackbarMessageText,
                    duration = snackbarMessage.duration,
                )
                // The snackbar item was displayed and dismissed, clear its message
                dispatcher.clear()
            } catch (e: CancellationException) {
                // The snackbar was being displayed when the coroutine was cancelled,
                // so we need to clear its message
                dispatcher.clear()
                throw e
            }
        }
    }
    return snackbarHostState
}
