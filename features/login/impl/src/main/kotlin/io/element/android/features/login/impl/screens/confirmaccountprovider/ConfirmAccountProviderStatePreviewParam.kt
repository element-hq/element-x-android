/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.changeserver.aChangeServerState
import io.element.android.features.login.impl.login.LoginModeState
import io.element.android.features.login.impl.login.aLoginModeState
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.libraries.architecture.AsyncData

open class ConfirmAccountProviderStatePreviewParam : PreviewParameterProvider<ConfirmAccountProviderState> {
    override val values: Sequence<ConfirmAccountProviderState>
        get() = sequenceOf(
            aConfirmAccountProviderState(),
            aConfirmAccountProviderState(
                accountProviderInput = "element",
                accountProviderSuggestion = "element.io",
            ),
            aConfirmAccountProviderState(
                isAccountCreation = true,
            ),
            aConfirmAccountProviderState(
                isAccountCreation = true,
                loginModeState = aLoginModeState(loginMode = AsyncData.Failure(AccountCreationNotSupported())),
            ),
        )
}

private fun aConfirmAccountProviderState(
    accountProviderInput: String = "matrix.org",
    accountProviderSuggestion: String? = null,
    isAccountCreation: Boolean = false,
    loginModeState: LoginModeState = aLoginModeState(),
    changeServerState: ChangeServerState = aChangeServerState(),
    eventSink: (ConfirmAccountProviderEvent) -> Unit = {},
) = ConfirmAccountProviderState(
    accountProviderInput = accountProviderInput,
    accountProviderSuggestion = accountProviderSuggestion,
    isAccountCreation = isAccountCreation,
    loginModeState = loginModeState,
    changeServerState = changeServerState,
    eventSink = eventSink
)
