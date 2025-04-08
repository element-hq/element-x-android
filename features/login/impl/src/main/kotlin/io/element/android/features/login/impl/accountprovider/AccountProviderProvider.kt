/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.login.impl.R

open class AccountProviderProvider : PreviewParameterProvider<AccountProvider> {
    override val values: Sequence<AccountProvider>
        get() = sequenceOf(
            anAccountProvider(),
            anAccountProvider().copy(subtitleResourceId = null),
            anAccountProvider().copy(subtitleResourceId = null, title = "invalid", isValid = false),
            anAccountProvider().copy(subtitleResourceId = null, title = "Other", isPublic = false, isMatrixOrg = false),
            // Add other state here
        )
}

fun anAccountProvider() = AccountProvider(
    url = AuthenticationConfig.MATRIX_ORG_URL,
    subtitleResourceId = R.string.screen_change_account_provider_matrix_org_subtitle,
    isPublic = true,
    isMatrixOrg = true,
    isValid = true,
)
