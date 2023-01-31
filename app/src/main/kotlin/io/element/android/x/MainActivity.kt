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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeComponentActivity
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.designsystem.ElementXTheme
import io.element.android.x.di.AppBindings
import io.element.android.x.node.RootFlowNode

class MainActivity : NodeComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appBindings = bindings<AppBindings>()
        appBindings.matrixClientsHolder().restore(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ElementXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NodeHost(integrationPoint = appyxIntegrationPoint) {
                        RootFlowNode(
                            buildContext = it,
                            appComponentOwner = applicationContext as DaggerComponentOwner,
                            authenticationService = appBindings.authenticationService(),
                            presenter = appBindings.rootPresenter(),
                            matrixClientsHolder = appBindings.matrixClientsHolder()
                        )
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        bindings<AppBindings>().matrixClientsHolder().onSaveInstanceState(outState)
    }
}
