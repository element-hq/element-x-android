/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
