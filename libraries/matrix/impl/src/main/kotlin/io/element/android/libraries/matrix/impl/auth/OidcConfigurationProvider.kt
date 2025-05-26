/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.auth.OidcConfig
import io.element.android.libraries.matrix.api.auth.OidcRedirectUrlProvider
import org.matrix.rustcomponents.sdk.OidcConfiguration
import javax.inject.Inject

class OidcConfigurationProvider @Inject constructor(
    private val buildMeta: BuildMeta,
    private val oidcRedirectUrlProvider: OidcRedirectUrlProvider,
) {
    fun get(): OidcConfiguration = OidcConfiguration(
        clientName = buildMeta.applicationName,
        redirectUri = oidcRedirectUrlProvider.provide(),
        clientUri = OidcConfig.CLIENT_URI,
        logoUri = OidcConfig.LOGO_URI,
        tosUri = OidcConfig.TOS_URI,
        policyUri = OidcConfig.POLICY_URI,
        staticRegistrations = OidcConfig.STATIC_REGISTRATIONS,
    )
}
