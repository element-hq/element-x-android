/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl

import androidx.core.net.toUri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.login.api.LoginIntentResolver
import io.element.android.features.login.api.LoginParams
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultLoginIntentResolver @Inject constructor() : LoginIntentResolver {
    override fun parse(uriString: String): LoginParams? {
        val uri = uriString.toUri()
        if (uri.host != "mobile.element.io") return null
        if (uri.path.orEmpty().startsWith("/element").not()) return null
        val accountProvider = uri.getQueryParameter("account_provider") ?: return null
        val loginHint = uri.getQueryParameter("login_hint")
        return LoginParams(
            accountProvider = accountProvider,
            loginHint = loginHint,
        )
    }
}
