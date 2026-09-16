/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accesscontrol

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.api.IsEnterpriseBuild
import io.element.android.features.login.api.accesscontrol.AccountProviderAccessControl
import io.element.android.features.login.impl.changeserver.AccountProviderAccessException
import io.element.android.libraries.matrix.api.accountprovider.AccountProvider

@ContributesBinding(AppScope::class)
class DefaultAccountProviderAccessControl(
    private val isEnterpriseBuild: IsEnterpriseBuild,
    private val enterpriseService: EnterpriseService,
) : AccountProviderAccessControl {
    override suspend fun isAllowedToConnectToAccountProvider(accountProvider: AccountProvider) = try {
        assertIsAllowedToConnectToAccountProvider(
            accountProvider = accountProvider,
        )
        true
    } catch (_: AccountProviderAccessException) {
        false
    }

    @Throws(AccountProviderAccessException::class)
    suspend fun assertIsAllowedToConnectToAccountProvider(
        accountProvider: AccountProvider,
    ) {
        if (isEnterpriseBuild().not()) {
            // Ensure that Element Pro is not required for this account provider
            if (enterpriseService.isElementProEnforced(accountProvider.serverNameOrBaseUrl())) {
                throw AccountProviderAccessException.NeedElementProException(
                    unauthorisedAccountProviderTitle = accountProvider.friendlyServerName(),
                    applicationId = ELEMENT_PRO_APPLICATION_ID,
                )
            }
        }
        if (enterpriseService.isAllowedToConnectToAccountProvider(accountProvider).not()) {
            throw AccountProviderAccessException.UnauthorizedAccountProviderException(
                unauthorisedAccountProviderTitle = accountProvider.friendlyServerName(),
                authorisedAccountProviderTitles = enterpriseService.accountProviderAllowList()
                    .map { it.friendlyServerName() },
            )
        }
    }

    companion object {
        const val ELEMENT_PRO_APPLICATION_ID = "io.element.enterprise"
    }
}
