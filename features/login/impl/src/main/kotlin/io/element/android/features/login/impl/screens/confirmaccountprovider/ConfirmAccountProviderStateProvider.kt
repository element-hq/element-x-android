/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.accountprovider.anAccountProvider
import io.element.android.libraries.architecture.AsyncData

open class ConfirmAccountProviderStateProvider : PreviewParameterProvider<ConfirmAccountProviderState> {
    override val values: Sequence<ConfirmAccountProviderState>
        get() = sequenceOf(
            aConfirmAccountProviderState(),
            // Add other state here
        )
}

fun aConfirmAccountProviderState() = ConfirmAccountProviderState(
    accountProvider = anAccountProvider(),
    isAccountCreation = false,
    loginFlow = AsyncData.Uninitialized,
    eventSink = {}
)
