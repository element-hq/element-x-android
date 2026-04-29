/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.auth.OAuthConfig
import io.element.android.libraries.matrix.api.auth.OAuthRedirectUrlProvider
import org.matrix.rustcomponents.sdk.OAuthConfiguration

@Inject
class OAuthConfigurationProvider(
    private val buildMeta: BuildMeta,
    private val oAuthRedirectUrlProvider: OAuthRedirectUrlProvider,
) {
    fun get(): OAuthConfiguration = OAuthConfiguration(
        clientName = buildMeta.applicationName,
        redirectUri = oAuthRedirectUrlProvider.provide(),
        clientUri = OAuthConfig.CLIENT_URI,
        logoUri = OAuthConfig.LOGO_URI,
        tosUri = OAuthConfig.TOS_URI,
        policyUri = OAuthConfig.POLICY_URI,
        staticRegistrations = OAuthConfig.STATIC_REGISTRATIONS,
    )
}
