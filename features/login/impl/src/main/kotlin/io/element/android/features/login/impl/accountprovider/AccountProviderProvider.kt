/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.appconfig.AuthenticationConfig

open class AccountProviderProvider : PreviewParameterProvider<AccountProvider> {
    override val values: Sequence<AccountProvider>
        get() = sequenceOf(
            anAccountProvider(),
            anAccountProvider().copy(subtitle = null),
            anAccountProvider().copy(subtitle = null, title = "invalid", isValid = false),
            anAccountProvider().copy(subtitle = null, title = "Other", isPublic = false, isMatrixOrg = false),
            // Add other state here
        )
}

fun anAccountProvider() = AccountProvider(
    url = AuthenticationConfig.MATRIX_ORG_URL,
    subtitle = "Matrix.org is an open network for secure, decentralized communication.",
    isPublic = true,
    isMatrixOrg = true,
    isValid = true,
)
