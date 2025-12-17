/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl

import android.content.Intent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcIntentResolver

@ContributesBinding(AppScope::class)
class DefaultOidcIntentResolver(
    private val oidcUrlParser: OidcUrlParser,
) : OidcIntentResolver {
    override fun resolve(intent: Intent): OidcAction? {
        return oidcUrlParser.parse(intent.dataString.orEmpty())
    }
}
