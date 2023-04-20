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
import org.matrix.rustcomponents.sdk.OidcClientMetadata

val oidcClientMetadata: OidcClientMetadata = OidcClientMetadata(
    clientName = "Element",
    redirectUri = OidcConfig.redirectUri,
    clientUri = "https://element.io",
    tosUri = "https://element.io/user-terms-of-service",
    policyUri = "https://element.io/privacy"
)

