/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oauth.impl

import android.content.Intent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.oauth.api.OAuthAction
import io.element.android.libraries.oauth.api.OAuthIntentResolver

@ContributesBinding(AppScope::class)
class DefaultOAuthIntentResolver(
    private val oAuthUrlParser: OAuthUrlParser,
) : OAuthIntentResolver {
    override fun resolve(intent: Intent): OAuthAction? {
        return oAuthUrlParser.parse(intent.dataString.orEmpty())
    }
}
