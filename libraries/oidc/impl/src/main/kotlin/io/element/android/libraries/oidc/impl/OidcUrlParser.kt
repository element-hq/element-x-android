/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl

import io.element.android.libraries.matrix.api.auth.OidcConfig
import io.element.android.libraries.oidc.api.OidcAction
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
