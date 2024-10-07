/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.matrix.impl.RustClientBuilderProvider
import io.element.android.libraries.matrix.impl.RustMatrixClientFactory
import io.element.android.libraries.matrix.impl.auth.OidcConfigurationProvider
import io.element.android.libraries.matrix.impl.auth.RustMatrixAuthenticationService
import io.element.android.libraries.matrix.impl.paths.SessionPathsFactory
import io.element.android.libraries.matrix.impl.room.RustTimelineEventTypeFilterFactory
import io.element.android.libraries.network.useragent.SimpleUserAgentProvider
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.services.analytics.noop.NoopAnalyticsService
import io.element.android.services.toolbox.impl.systemclock.DefaultSystemClock
import kotlinx.coroutines.runBlocking
import java.io.File

class MainActivity : ComponentActivity() {
    private val matrixAuthenticationService: MatrixAuthenticationService by lazy {
        val baseDirectory = File(applicationContext.filesDir, "sessions")
        val userAgentProvider = SimpleUserAgentProvider("MinimalSample")
        val sessionStore = InMemorySessionStore()
        val userCertificatesProvider = NoOpUserCertificatesProvider()
        val proxyProvider = NoOpProxyProvider()
        RustMatrixAuthenticationService(
            sessionPathsFactory = SessionPathsFactory(baseDirectory, applicationContext.cacheDir),
            coroutineDispatchers = Singleton.coroutineDispatchers,
            sessionStore = sessionStore,
            rustMatrixClientFactory = RustMatrixClientFactory(
                baseDirectory = baseDirectory,
                cacheDirectory = applicationContext.cacheDir,
                appCoroutineScope = Singleton.appScope,
                coroutineDispatchers = Singleton.coroutineDispatchers,
                sessionStore = sessionStore,
                userAgentProvider = userAgentProvider,
                userCertificatesProvider = userCertificatesProvider,
                proxyProvider = proxyProvider,
                clock = DefaultSystemClock(),
                analyticsService = NoopAnalyticsService(),
                featureFlagService = AlwaysEnabledFeatureFlagService(),
                timelineEventTypeFilterFactory = RustTimelineEventTypeFilterFactory(),
                clientBuilderProvider = RustClientBuilderProvider(),
            ),
            passphraseGenerator = NullPassphraseGenerator(),
            oidcConfigurationProvider = OidcConfigurationProvider(baseDirectory),
            appPreferencesStore = InMemoryAppPreferencesStore(),
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
