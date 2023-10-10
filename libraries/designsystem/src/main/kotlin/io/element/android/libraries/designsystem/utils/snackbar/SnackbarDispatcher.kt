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

    suspend fun post(message: SnackbarMessage) {
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
val LocalSnackbarDispatcher = compositionLocalOf<SnackbarDispatcher> { SnackbarDispatcher() }

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
    LaunchedEffect(snackbarMessageText) {
        // If the message wasn't already displayed, do it now, and mark it as displayed
        // This will prevent the message from appearing in any other active SnackbarHosts
        if (snackbarMessage.isDisplayed.getAndSet(true) == false) {
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
