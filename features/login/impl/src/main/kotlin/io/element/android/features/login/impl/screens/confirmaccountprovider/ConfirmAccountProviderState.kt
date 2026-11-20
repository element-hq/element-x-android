/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.login.LoginModeState
import io.element.android.libraries.architecture.AsyncData

data class ConfirmAccountProviderState(
    val accountProviderInput: String,
    // The full account provider from history that the current input is a prefix of, offered as inline autocomplete.
    val accountProviderSuggestion: String?,
    val isAccountCreation: Boolean,
    val loginModeState: LoginModeState,
    val changeServerState: ChangeServerState,
    val eventSink: (ConfirmAccountProviderEvent) -> Unit
) {
    val submitEnabled: Boolean
        get() = accountProviderInput.isNotBlank() &&
            loginModeState.loginMode is AsyncData.Uninitialized &&
            changeServerState.changeServerAction is AsyncData.Uninitialized

    val isLoading: Boolean
        get() = loginModeState.loginMode is AsyncData.Loading ||
            changeServerState.changeServerAction is AsyncData.Loading

    val isShowingError: Boolean
        get() = loginModeState.loginMode is AsyncData.Failure ||
            changeServerState.changeServerAction is AsyncData.Failure
}
