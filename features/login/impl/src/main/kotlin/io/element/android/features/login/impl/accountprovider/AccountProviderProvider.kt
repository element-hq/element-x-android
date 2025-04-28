/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class AccountProviderProvider : PreviewParameterProvider<AccountProvider> {
    private val longAccountProvider = AccountProvider(
        url = "https://default-title.for.public-server.at.really-long-url.with.many.subdomains.co.uk",
        isPublic = true
    )
    override val values: Sequence<AccountProvider>
        get() = sequenceOf(
            anAccountProvider(),
            anAccountProvider().copy(descriptionResourceId = null),
            longAccountProvider,
            longAccountProvider.copy(title = "custom title"),
            anAccountProvider().copy(descriptionResourceId = null, title = "Other", isPublic = false),
        )
}

fun anAccountProvider() = AccountProviderDataSource().matrixOrgAccountProvider
