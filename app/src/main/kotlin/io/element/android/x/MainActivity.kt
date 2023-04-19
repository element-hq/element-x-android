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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeComponentActivity
import com.bumble.appyx.core.plugin.NodeReadyObserver
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.designsystem.compound.CompoundTheme
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.x.di.AppBindings
import timber.log.Timber

private val loggerTag = LoggerTag("MainActivity")

class MainActivity : NodeComponentActivity() {

    lateinit var mainNode: MainNode

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(loggerTag.value).w("onCreate, with savedInstanceState: ${savedInstanceState != null}")
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val appBindings = bindings<AppBindings>()
        appBindings.matrixClientsHolder().restore(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CompoundTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    NodeHost(integrationPoint = appyxIntegrationPoint) {
                        MainNode(
                            it,
                            appBindings.mainDaggerComponentOwner(),
                            plugins = listOf(
                                object : NodeReadyObserver<MainNode> {
                                    override fun init(node: MainNode) {
                                        mainNode = node
                                        mainNode.handleIntent(intent)
                                    }
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Called when:
     * - the launcher icon is clicked (if the app is already running);
     * - a notification is clicked.
     * - the app is going to background (<- this is strange)
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.tag(loggerTag.value).w("onNewIntent")
        intent ?: return
        mainNode.handleIntent(intent)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        bindings<AppBindings>().matrixClientsHolder().onSaveInstanceState(outState)
    }
}
