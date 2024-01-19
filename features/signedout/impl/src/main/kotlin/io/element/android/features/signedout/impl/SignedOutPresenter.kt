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
        val sessions by sessionStore.sessionsFlow().collectAsState(initial = emptyList())
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
