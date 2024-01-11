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

package io.element.android.features.login.impl.oidc

import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.libraries.matrix.api.auth.OidcConfig
import javax.inject.Inject

/**
 * Simple parser for oidc url interception.
 * TODO Find documentation about the format.
 */
class OidcUrlParser @Inject constructor() {
    /**
     * Return a OidcAction, or null if the url is not a OidcUrl.
     * Note:
     * When user press button "Cancel", we get the url:
     * `io.element:/callback?error=access_denied&state=IFF1UETGye2ZA8pO`
     * On success, we get:
     * `io.element:/callback?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB`
     */
    fun parse(url: String): OidcAction? {
        if (url.startsWith(OidcConfig.REDIRECT_URI).not()) return null
        if (url.contains("error=access_denied")) return OidcAction.GoBack
        if (url.contains("code=")) return OidcAction.Success(url)

        // Other case not supported, let's crash the app for now
        error("Not supported: $url")
    }
}
