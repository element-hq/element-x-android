/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.appconfig.AuthenticationConfig

open class AccountProviderProvider : PreviewParameterProvider<AccountProvider> {
    private val longAccountProvider = AccountProvider(url = "https://default-title.for.really-long-url.with.many.subdomains.co.uk")
    override val values: Sequence<AccountProvider>
        get() = sequenceOf(
            anAccountProvider(),
            anAccountProvider().copy(descriptionResourceId = null),
            longAccountProvider,
            longAccountProvider.copy(title = "custom title"),
            anAccountProvider().copy(descriptionResourceId = null, title = "Other", isPublic = false),
        )
}

fun anAccountProvider() = AccountProvider(
    url = AuthenticationConfig.MATRIX_ORG_URL,
    isPublic = true,
)
