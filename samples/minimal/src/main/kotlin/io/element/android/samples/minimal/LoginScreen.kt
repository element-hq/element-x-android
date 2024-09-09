/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.samples.minimal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.screens.loginpassword.LoginPasswordPresenter
import io.element.android.features.login.impl.screens.loginpassword.LoginPasswordView
import io.element.android.features.login.impl.util.defaultAccountProvider
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService

class LoginScreen(private val authenticationService: MatrixAuthenticationService) {
    @Composable
    fun Content(modifier: Modifier = Modifier) {
        val presenter = remember {
            LoginPasswordPresenter(
                authenticationService = authenticationService,
                AccountProviderDataSource(),
                DefaultLoginUserStory(),
            )
        }

        LaunchedEffect(Unit) {
            authenticationService.setHomeserver(defaultAccountProvider.url)
        }

        val state = presenter.present()
        LoginPasswordView(
            state = state,
            modifier = modifier,
            onBackClick = {},
        )
    }
}
