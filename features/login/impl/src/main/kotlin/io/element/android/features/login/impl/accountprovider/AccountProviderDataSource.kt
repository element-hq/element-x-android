/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@SingleIn(AppScope::class)
class AccountProviderDataSource @Inject constructor(
    enterpriseService: EnterpriseService,
) {
    private val defaultAccountProvider =
        (enterpriseService.defaultHomeserverList().firstOrNull { it != EnterpriseService.ANY_ACCOUNT_PROVIDER } ?: AuthenticationConfig.MATRIX_ORG_URL)
            .let { url ->
                AccountProvider(
                    url = url,
                    subtitle = null,
                    isPublic = url == AuthenticationConfig.MATRIX_ORG_URL,
                    isMatrixOrg = url == AuthenticationConfig.MATRIX_ORG_URL,
                )
            }

    private val accountProvider: MutableStateFlow<AccountProvider> = MutableStateFlow(
        defaultAccountProvider
    )

    val flow: StateFlow<AccountProvider> = accountProvider.asStateFlow()

    fun reset() {
        accountProvider.tryEmit(defaultAccountProvider)
    }

    fun userSelection(data: AccountProvider) {
        accountProvider.tryEmit(data)
    }
}
