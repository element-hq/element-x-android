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
import io.element.android.features.login.api.accesscontrol.AccountProviderAccessControl
import io.element.android.features.login.impl.changeserver.AccountProviderAccessException
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.wellknown.api.WellknownRetriever

@ContributesBinding(AppScope::class)
class DefaultAccountProviderAccessControl(
    private val enterpriseService: EnterpriseService,
    private val wellknownRetriever: WellknownRetriever,
) : AccountProviderAccessControl {
    override suspend fun isAllowedToConnectToAccountProvider(accountProviderUrl: String) = try {
        assertIsAllowedToConnectToAccountProvider(
            title = accountProviderUrl,
            accountProviderUrl = accountProviderUrl,
        )
        true
    } catch (_: AccountProviderAccessException) {
        false
    }

    @Throws(AccountProviderAccessException::class)
    suspend fun assertIsAllowedToConnectToAccountProvider(
        title: String,
        accountProviderUrl: String,
    ) {
        if (enterpriseService.isEnterpriseBuild.not()) {
            // Ensure that Element Pro is not required for this account provider
            val wellKnown = wellknownRetriever.getElementWellKnown(
                baseUrl = accountProviderUrl.ensureProtocol(),
            ).dataOrNull()
            if (wellKnown?.enforceElementPro == true) {
                throw AccountProviderAccessException.NeedElementProException(
                    unauthorisedAccountProviderTitle = title,
                    applicationId = ELEMENT_PRO_APPLICATION_ID,
                )
            }
        }
        if (enterpriseService.isAllowedToConnectToHomeserver(accountProviderUrl).not()) {
            throw AccountProviderAccessException.UnauthorizedAccountProviderException(
                unauthorisedAccountProviderTitle = title,
                authorisedAccountProviderTitles = enterpriseService.defaultHomeserverList(),
            )
        }
    }

    companion object {
        const val ELEMENT_PRO_APPLICATION_ID = "io.element.enterprise"
    }
}
