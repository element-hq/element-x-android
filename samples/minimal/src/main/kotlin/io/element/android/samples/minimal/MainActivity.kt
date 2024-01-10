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

package io.element.android.samples.minimal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.impl.RustMatrixClientFactory
import io.element.android.libraries.matrix.impl.auth.RustMatrixAuthenticationService
import io.element.android.libraries.network.useragent.SimpleUserAgentProvider
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.services.toolbox.impl.systemclock.DefaultSystemClock
import kotlinx.coroutines.runBlocking
import java.io.File

class MainActivity : ComponentActivity() {

    private val matrixAuthenticationService: MatrixAuthenticationService by lazy {
        val baseDirectory = File(applicationContext.filesDir, "sessions")
        val userAgentProvider = SimpleUserAgentProvider("MinimalSample")
        val sessionStore = InMemorySessionStore()
        RustMatrixAuthenticationService(
            baseDirectory = baseDirectory,
            coroutineDispatchers = Singleton.coroutineDispatchers,
            sessionStore = sessionStore,
            userAgentProvider = userAgentProvider,
            rustMatrixClientFactory = RustMatrixClientFactory(
                baseDirectory = baseDirectory,
                cacheDirectory = applicationContext.cacheDir,
                appCoroutineScope = Singleton.appScope,
                coroutineDispatchers = Singleton.coroutineDispatchers,
                sessionStore = sessionStore,
                userAgentProvider = userAgentProvider,
                clock = DefaultSystemClock(),
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ElementTheme {
                val loggedInState by matrixAuthenticationService.loggedInStateFlow().collectAsState(initial = LoggedInState.NotLoggedIn)
                Content(isLoggedIn = loggedInState is LoggedInState.LoggedIn, modifier = Modifier.fillMaxSize())
            }
        }
    }

    @Composable
    fun Content(
        isLoggedIn: Boolean,
        modifier: Modifier = Modifier
    ) {
        if (!isLoggedIn) {
            LoginScreen(authenticationService = matrixAuthenticationService).Content(modifier)
        } else {
            val matrixClient = runBlocking {
                val sessionId = matrixAuthenticationService.getLatestSessionId()!!
                matrixAuthenticationService.restoreSession(sessionId).getOrNull()
            }
            RoomListScreen(LocalContext.current, matrixClient!!).Content(modifier)
        }
    }
}
