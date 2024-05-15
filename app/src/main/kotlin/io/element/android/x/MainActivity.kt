/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeActivity
import com.bumble.appyx.core.plugin.NodeReadyObserver
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.isDark
import io.element.android.compound.theme.mapToTheme
import io.element.android.features.lockscreen.api.handleSecureFlag
import io.element.android.features.lockscreen.api.isLocked
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.designsystem.utils.snackbar.LocalSnackbarDispatcher
import io.element.android.x.di.AppBindings
import io.element.android.x.intent.SafeUriHandler
import timber.log.Timber

private val loggerTag = LoggerTag("MainActivity")

class MainActivity : NodeActivity() {
    private lateinit var mainNode: MainNode
    private lateinit var appBindings: AppBindings

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(loggerTag.value).w("onCreate, with savedInstanceState: ${savedInstanceState != null}")
        installSplashScreen()
        super.onCreate(savedInstanceState)
        appBindings = bindings()
        appBindings.lockScreenService().handleSecureFlag(this)
        enableEdgeToEdge()
        setContent {
            MainContent(appBindings)
        }
    }

    @Deprecated("")
    override fun onBackPressed() {
        // If the app is locked, we need to intercept onBackPressed before it goes to OnBackPressedDispatcher.
        // Indeed, otherwise we would need to trick Appyx backstack management everywhere.
        // Without this trick, we would get pop operations on the hidden backstack.
        if (appBindings.lockScreenService().isLocked) {
            // Do not kill the app in this case, just go to background.
            moveTaskToBack(false)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    @Composable
    private fun MainContent(appBindings: AppBindings) {
        val theme by remember {
            appBindings.preferencesStore().getThemeFlow().mapToTheme()
        }
            .collectAsState(initial = Theme.System)
        val migrationState = appBindings.migrationEntryPoint().present()
        ElementTheme(
            darkTheme = theme.isDark()
        ) {
            CompositionLocalProvider(
                LocalSnackbarDispatcher provides appBindings.snackbarDispatcher(),
                LocalUriHandler provides SafeUriHandler(this),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    if (migrationState.migrationAction.isSuccess()) {
                        MainNodeHost()
                    } else {
                        appBindings.migrationEntryPoint().Render(
                            state = migrationState,
                            modifier = Modifier,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun MainNodeHost() {
        NodeHost(integrationPoint = appyxIntegrationPoint) {
            MainNode(
                it,
                plugins = listOf(
                    object : NodeReadyObserver<MainNode> {
                        override fun init(node: MainNode) {
                            Timber.tag(loggerTag.value).w("onMainNodeInit")
                            mainNode = node
                            mainNode.handleIntent(intent)
                        }
                    }
                ),
                context = applicationContext
            )
        }
    }

    /**
     * Called when:
     * - the launcher icon is clicked (if the app is already running);
     * - a notification is clicked.
     * - a deep link have been clicked
     * - the app is going to background (<- this is strange)
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.tag(loggerTag.value).w("onNewIntent")
        // If the mainNode is not init yet, keep the intent for later.
        // It can happen when the activity is killed by the system. The methods are called in this order :
        // onCreate(savedInstanceState=true) -> onNewIntent -> onResume -> onMainNodeInit
        if (::mainNode.isInitialized) {
            mainNode.handleIntent(intent)
        } else {
            setIntent(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.tag(loggerTag.value).w("onPause")
    }

    override fun onResume() {
        super.onResume()
        Timber.tag(loggerTag.value).w("onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(loggerTag.value).w("onDestroy")
    }
}
