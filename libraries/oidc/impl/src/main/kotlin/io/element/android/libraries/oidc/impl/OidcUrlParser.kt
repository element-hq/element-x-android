/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.auth.OidcRedirectUrlProvider
import io.element.android.libraries.oidc.api.OidcAction

fun interface OidcUrlParser {
    fun parse(url: String): OidcAction?
}

/**
 * Simple parser for oidc url interception.
 * TODO Find documentation about the format.
 */
@ContributesBinding(AppScope::class)
class DefaultOidcUrlParser(
    private val oidcRedirectUrlProvider: OidcRedirectUrlProvider,
) : OidcUrlParser {
    /**
     * Return a OidcAction, or null if the url is not a OidcUrl.
     * Note:
     * When user press button "Cancel", we get the url:
     * `io.element.android:/?error=access_denied&state=IFF1UETGye2ZA8pO`
     * On success, we get:
     * `io.element.android:/?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB`
     */
    override fun parse(url: String): OidcAction? {
        if (url.startsWith(oidcRedirectUrlProvider.provide()).not()) return null
        if (url.contains("error=access_denied")) return OidcAction.GoBack()
        if (url.contains("code=")) return OidcAction.Success(url)

        // Other case not supported, let's crash the app for now
        error("Not supported: $url")
    }
}
