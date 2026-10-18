/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.accountprovider.AccountProvider

open class AccountProviderPreviewParam : PreviewParameterProvider<AccountProvider> {
    override val values: Sequence<AccountProvider>
        get() = sequenceOf(
            anAccountProviderManaged(),
        )
}

fun anAccountProviderManaged(
    serverName: String = "matrix.org",
    baseUrl: String = "https://matrix-client.matrix.org",
    canUseServerName: Boolean = true,
) = AccountProvider.Managed(
    serverName = serverName,
    baseUrl = baseUrl,
    canUseServerName = canUseServerName,
)

fun anAccountProviderGeneric(
    serverName: String = "matrix.org",
) = AccountProvider.Generic(
    serverName = serverName,
)
