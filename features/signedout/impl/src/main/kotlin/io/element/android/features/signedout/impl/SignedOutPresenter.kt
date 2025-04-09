/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.launch

class SignedOutPresenter @AssistedInject constructor(
    // Cannot inject SessionId
    @Assisted private val sessionId: String,
    private val sessionStore: SessionStore,
    private val buildMeta: BuildMeta,
) : Presenter<SignedOutState> {
    @AssistedFactory
    interface Factory {
        fun create(sessionId: String): SignedOutPresenter
    }

    @Composable
    override fun present(): SignedOutState {
        val sessions by remember {
            sessionStore.sessionsFlow()
        }.collectAsState(initial = emptyList())
        val signedOutSession by remember {
            derivedStateOf { sessions.firstOrNull { it.userId == sessionId } }
        }
        val coroutineScope = rememberCoroutineScope()

        fun handleEvents(event: SignedOutEvents) {
            when (event) {
                SignedOutEvents.SignInAgain -> coroutineScope.launch {
                    sessionStore.removeSession(sessionId)
                }
            }
        }

        return SignedOutState(
            appName = buildMeta.applicationName,
            signedOutSession = signedOutSession,
            eventSink = ::handleEvents
        )
    }
}
