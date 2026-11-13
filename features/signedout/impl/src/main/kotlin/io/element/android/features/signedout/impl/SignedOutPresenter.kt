/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingStateNoSuccess
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.toMatrixUser
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

@AssistedInject
class SignedOutPresenter(
    @Assisted private val sessionId: SessionId,
    private val sessionStore: SessionStore,
) : Presenter<SignedOutState> {
    @AssistedFactory
    fun interface Factory {
        fun create(sessionId: SessionId): SignedOutPresenter
    }

    @Composable
    override fun present(): SignedOutState {
        val signedOutSession by remember {
            sessionStore.sessionsFlow()
                // Ignore the removal of the session, to keep rendering the account data until the screen is closed
                .mapNotNull { sessions -> sessions.firstOrNull { it.userId == sessionId.value } }
                .map { it.toMatrixUser() }
        }.collectAsState(initial = MatrixUser(userId = sessionId))
        val submitAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val coroutineScope = rememberCoroutineScope()

        fun handleEvent(event: SignedOutEvent) {
            when (event) {
                SignedOutEvent.Submit -> {
                    if (submitAction.value.isLoading()) return
                    coroutineScope.removeSession(submitAction)
                }
            }
        }

        return SignedOutState(
            signedOutMatrixUser = signedOutSession,
            submitAction = submitAction.value,
            eventSink = ::handleEvent,
        )
    }

    /**
     * Once the session is removed, the parent flow closes the screen, so the action stays in the loading state on success.
     */
    private fun CoroutineScope.removeSession(submitAction: MutableState<AsyncAction<Unit>>) = launch {
        submitAction.runUpdatingStateNoSuccess {
            runCatchingExceptions {
                sessionStore.removeSession(sessionId.value)
            }
        }.onFailure {
            Timber.e(it, "Failed to remove signed out session $sessionId")
        }
    }
}
