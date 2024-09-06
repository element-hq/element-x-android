/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.api.auth.OidcConfig
import org.matrix.rustcomponents.sdk.OidcConfiguration
import java.io.File
import javax.inject.Inject

class OidcConfigurationProvider @Inject constructor(
    private val baseDirectory: File,
) {
    fun get(): OidcConfiguration = OidcConfiguration(
        clientName = "Element",
        redirectUri = OidcConfig.REDIRECT_URI,
        clientUri = "https://element.io",
        logoUri = "https://element.io/mobile-icon.png",
        tosUri = "https://element.io/acceptable-use-policy-terms",
        policyUri = "https://element.io/privacy",
        contacts = listOf(
            "support@element.io",
        ),
        // Some homeservers/auth issuers don't support dynamic client registration, and have to be registered manually
        staticRegistrations = mapOf(
            "https://id.thirdroom.io/realms/thirdroom" to "elementx",
        ),
        dynamicRegistrationsFile = File(baseDirectory, "oidc/registrations.json").absolutePath,
    )
}
