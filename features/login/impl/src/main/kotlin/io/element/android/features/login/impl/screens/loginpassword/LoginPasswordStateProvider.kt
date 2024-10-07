/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.loginpassword

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.accountprovider.anAccountProvider
import io.element.android.libraries.architecture.AsyncData

open class LoginPasswordStateProvider : PreviewParameterProvider<LoginPasswordState> {
    override val values: Sequence<LoginPasswordState>
        get() = sequenceOf(
            aLoginPasswordState(),
            // Loading
            aLoginPasswordState().copy(loginAction = AsyncData.Loading()),
            // Error
            aLoginPasswordState().copy(loginAction = AsyncData.Failure(Exception("An error occurred"))),
        )
}

fun aLoginPasswordState() = LoginPasswordState(
    accountProvider = anAccountProvider(),
    formState = LoginFormState.Default,
    loginAction = AsyncData.Uninitialized,
    eventSink = {}
)
