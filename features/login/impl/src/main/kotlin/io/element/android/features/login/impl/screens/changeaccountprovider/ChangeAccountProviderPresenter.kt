/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.changeaccountprovider

import androidx.compose.runtime.Composable
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class ChangeAccountProviderPresenter @Inject constructor(
    private val changeServerPresenter: Presenter<ChangeServerState>,
) : Presenter<ChangeAccountProviderState> {
    @Composable
    override fun present(): ChangeAccountProviderState {
        val changeServerState = changeServerPresenter.present()
        return ChangeAccountProviderState(
            // Just matrix.org by default for now
            accountProviders = listOf(
                AccountProvider(
                    url = AuthenticationConfig.MATRIX_ORG_URL,
                    subtitle = null,
                    isPublic = true,
                    isMatrixOrg = true,
                    isValid = true,
                )
            ),
            changeServerState = changeServerState,
        )
    }
}
