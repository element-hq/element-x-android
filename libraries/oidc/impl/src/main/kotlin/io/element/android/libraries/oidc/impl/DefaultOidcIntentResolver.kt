/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl

import android.content.Intent
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcIntentResolver
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultOidcIntentResolver @Inject constructor(
    private val oidcUrlParser: OidcUrlParser,
) : OidcIntentResolver {
    override fun resolve(intent: Intent): OidcAction? {
        return oidcUrlParser.parse(intent.dataString.orEmpty())
    }
}
