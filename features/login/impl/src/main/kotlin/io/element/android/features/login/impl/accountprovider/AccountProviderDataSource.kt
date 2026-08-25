/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.api.canConnectToAnyHomeserver
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SingleIn(AppScope::class)
@Inject
class AccountProviderDataSource(
    private val enterpriseService: EnterpriseService,
    private val appPreferencesStore: AppPreferencesStore,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) {
    // The provider used when the user has not selected one: an enterprise/MDM-configured provider,
    // else matrix.org. The most recently used provider (from history) can override it, see init.
    private val configuredAccountProvider = createAccountProvider(
        url = enterpriseService.homeserverAllowList()
            .firstOrNull { it != EnterpriseService.ANY_ACCOUNT_PROVIDER }
            ?: AuthenticationConfig.MATRIX_ORG_URL
    )

    private val accountProvider: MutableStateFlow<AccountProvider> = MutableStateFlow(configuredAccountProvider)

    val flow: StateFlow<AccountProvider> = accountProvider.asStateFlow()

    // The account provider the user last explicitly selected (via [setAccountProvider] / [setUrl]).
    // Unlike [flow], this is not recomputed by [reset], so it survives to be persisted to history on a
    // successful sign-in even when the login flow is torn down in between (e.g. across an OAuth round-trip).
    var lastSelectedAccountProviderUrl: String? = null
        private set

    init {
        // Seed the default from the last used provider, unless the user has already selected one.
        coroutineScope.launch {
            val default = defaultAccountProvider()
            accountProvider.update { current -> if (current == configuredAccountProvider) default else current }
        }
    }

    suspend fun reset() {
        accountProvider.emit(defaultAccountProvider())
    }

    /**
     * The provider to default to: the most recently used one from history when the user is free to
     * connect to any provider, otherwise the enterprise/MDM-configured provider.
     */
    private suspend fun defaultAccountProvider(): AccountProvider {
        if (!enterpriseService.canConnectToAnyHomeserver()) {
            return configuredAccountProvider
        }
        val lastUsedProvider = appPreferencesStore.getHomeserverHistoryFlow().first().firstOrNull()
        return lastUsedProvider?.let { createAccountProvider(it) } ?: configuredAccountProvider
    }

    suspend fun setUrl(url: String) {
        setAccountProvider(createAccountProvider(url))
    }

    suspend fun setAccountProvider(data: AccountProvider) {
        lastSelectedAccountProviderUrl = data.url
        accountProvider.emit(data)
    }

    private fun createAccountProvider(url: String): AccountProvider {
        return AccountProvider(
            url = url,
            subtitle = null,
            isPublic = url == AuthenticationConfig.MATRIX_ORG_URL,
            isMatrixOrg = url == AuthenticationConfig.MATRIX_ORG_URL,
        )
    }
}
