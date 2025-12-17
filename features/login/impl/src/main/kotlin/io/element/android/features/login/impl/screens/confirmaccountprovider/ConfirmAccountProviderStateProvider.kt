/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.anAccountProvider
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.libraries.architecture.AsyncData

open class ConfirmAccountProviderStateProvider : PreviewParameterProvider<ConfirmAccountProviderState> {
    override val values: Sequence<ConfirmAccountProviderState>
        get() = sequenceOf(
            aConfirmAccountProviderState(),
            aConfirmAccountProviderState(
                isAccountCreation = true,
            ),
            aConfirmAccountProviderState(
                isAccountCreation = true,
                loginMode = AsyncData.Failure(AccountCreationNotSupported())
            ),
        )
}

private fun aConfirmAccountProviderState(
    accountProvider: AccountProvider = anAccountProvider(),
    isAccountCreation: Boolean = false,
    loginMode: AsyncData<LoginMode> = AsyncData.Uninitialized,
    eventSink: (ConfirmAccountProviderEvents) -> Unit = {},
) = ConfirmAccountProviderState(
    accountProvider = accountProvider,
    isAccountCreation = isAccountCreation,
    loginMode = loginMode,
    eventSink = eventSink
)
