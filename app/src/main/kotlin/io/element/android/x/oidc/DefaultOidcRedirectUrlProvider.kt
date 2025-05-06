/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.oidc

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.OidcRedirectUrlProvider
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.x.R
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultOidcRedirectUrlProvider @Inject constructor(
    private val stringProvider: StringProvider,
) : OidcRedirectUrlProvider {
    override fun provide() = buildString {
        append(stringProvider.getString(R.string.login_redirect_scheme))
        append(":/")
    }
}
