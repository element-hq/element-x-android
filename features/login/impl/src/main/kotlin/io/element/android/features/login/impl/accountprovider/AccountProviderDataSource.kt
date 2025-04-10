/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.login.impl.R
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@SingleIn(AppScope::class)
class AccountProviderDataSource @Inject constructor(
    enterpriseService: EnterpriseService? = null,
) {
    val matrixOrgAccountProvider = AccountProvider (
        url = AuthenticationConfig.MATRIX_ORG_URL,
        descriptionResourceId = R.string.screen_change_account_provider_matrix_org_subtitle,
        isPublic = true,
    )
    //add more hard coded AccountProvider here if you need them

    private val defaultAccountProvider = if (enterpriseService?.defaultHomeserver() != null
        && enterpriseService.defaultHomeserver() != AuthenticationConfig.MATRIX_ORG_URL) {
        AccountProvider(
            enterpriseService.defaultHomeserver()!!,
        )
    } else {
        matrixOrgAccountProvider
    }

    private val accountProvider: MutableStateFlow<AccountProvider> = MutableStateFlow(
        defaultAccountProvider
    )

    val accountProvidersList = listOf(
        defaultAccountProvider,
        matrixOrgAccountProvider
    ).distinct()

    val flow: StateFlow<AccountProvider> = accountProvider.asStateFlow()

    fun reset() {
        accountProvider.tryEmit(defaultAccountProvider)
    }

    fun userSelection(data: AccountProvider) {
        accountProvider.tryEmit(data)
    }
}
