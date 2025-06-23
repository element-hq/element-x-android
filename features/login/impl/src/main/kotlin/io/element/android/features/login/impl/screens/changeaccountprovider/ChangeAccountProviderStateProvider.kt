/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.changeaccountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.anAccountProvider
import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.changeserver.aChangeServerState

open class ChangeAccountProviderStateProvider : PreviewParameterProvider<ChangeAccountProviderState> {
    override val values: Sequence<ChangeAccountProviderState>
        get() = sequenceOf(
            aChangeAccountProviderState(),
            aChangeAccountProviderState(canSearchForAccountProviders = false),
            // Add other state here
        )
}

fun aChangeAccountProviderState(
    accountProviders: List<AccountProvider> = listOf(
        anAccountProvider()
    ),
    canSearchForAccountProviders: Boolean = true,
    changeServerState: ChangeServerState = aChangeServerState(),
) = ChangeAccountProviderState(
    accountProviders = accountProviders,
    canSearchForAccountProviders = canSearchForAccountProviders,
    changeServerState = changeServerState,
)
