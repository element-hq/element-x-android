/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class CreateAccountPresenter(
    @Assisted private val url: String,
    private val authenticationService: MatrixAuthenticationService,
    private val clientProvider: MatrixClientProvider,
    private val messageParser: MessageParser,
    private val buildMeta: BuildMeta,
) : Presenter<CreateAccountState> {
    @AssistedFactory
    interface Factory {
        fun create(url: String): CreateAccountPresenter
    }

    @Composable
    override fun present(): CreateAccountState {
        val coroutineScope = rememberCoroutineScope()
        val pageProgress: MutableState<Int> = remember { mutableIntStateOf(0) }
        val createAction: MutableState<AsyncAction<SessionId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvent(event: CreateAccountEvents) {
            when (event) {
                is CreateAccountEvents.SetPageProgress -> {
                    pageProgress.value = event.progress
                }
                is CreateAccountEvents.OnMessageReceived -> {
                    // Ignore unexpected message
                    if (event.message.contains("isTrusted")) return
                    coroutineScope.importSession(event.message, createAction)
                }
            }
        }

        return CreateAccountState(
            url = url,
            pageProgress = pageProgress.value,
            isDebugBuild = buildMeta.isDebuggable,
            createAction = createAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.importSession(message: String, loggedInState: MutableState<AsyncAction<SessionId>>) = launch {
        loggedInState.value = AsyncAction.Loading
        runCatchingExceptions {
            messageParser.parse(message)
        }.flatMap { externalSession ->
            authenticationService.importCreatedSession(externalSession)
        }.onSuccess { sessionId ->
            tryOrNull {
                // Wait until the session is verified
                val client = clientProvider.getOrRestore(sessionId).getOrThrow()
                val sessionVerificationService = client.sessionVerificationService
                withTimeout(10.seconds) { sessionVerificationService.sessionVerifiedStatus.first { it.isVerified() } }
            }
            loggedInState.value = AsyncAction.Success(sessionId)
        }.onFailure { failure ->
            loggedInState.value = AsyncAction.Failure(failure)
        }
    }
}
