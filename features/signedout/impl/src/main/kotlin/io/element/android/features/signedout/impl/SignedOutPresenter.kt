/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

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
        val coroutineScope = rememberCoroutineScope()

        fun handleEvent(event: SignedOutEvent) {
            when (event) {
                SignedOutEvent.Submit -> coroutineScope.launch {
                    sessionStore.removeSession(sessionId.value)
                }
            }
        }

        return SignedOutState(
            signedOutMatrixUser = signedOutSession,
            eventSink = ::handleEvent,
        )
    }

    private fun SessionData.toMatrixUser() = MatrixUser(
        userId = sessionId,
        displayName = userDisplayName,
        avatarUrl = userAvatarUrl,
        avatarThumbnail = userAvatarData,
    )
}
