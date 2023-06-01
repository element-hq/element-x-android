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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.element.android.features.login.impl.oidc.customtab.DefaultOidcActionFlow
import io.element.android.features.login.impl.root.LoginRootPresenter
import io.element.android.features.login.impl.root.LoginRootView
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService

class LoginScreen(private val authenticationService: MatrixAuthenticationService) {

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        val presenter = remember {
            LoginRootPresenter(
                authenticationService = authenticationService,
                DefaultOidcActionFlow()
            )
        }
        val state = presenter.present()
        LoginRootView(
            state = state,
            modifier = modifier,
            onBackPressed = {},
        )
    }
}
