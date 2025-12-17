/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.anAccountProvider
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.toImmutableList

open class ChooseAccountProviderStateProvider : PreviewParameterProvider<ChooseAccountProviderState> {
    private val server1 = anAccountProvider(
        url = "https://server1.io",
        subtitle = null,
        isPublic = false,
        isMatrixOrg = false,
    )
    private val server2 = anAccountProvider(
        url = "https://server2.io",
        subtitle = null,
        isPublic = false,
        isMatrixOrg = false,
    )
    private val server3 = anAccountProvider(
        url = "https://server3.io",
        subtitle = null,
        isPublic = false,
        isMatrixOrg = false,
    )
    override val values: Sequence<ChooseAccountProviderState>
        get() = sequenceOf(
            aChooseAccountProviderState(
                accountProviders = listOf(
                    server1,
                    server2,
                    server3,
                )
            ),
            aChooseAccountProviderState(
                accountProviders = listOf(
                    server1,
                    server2,
                    server3,
                ),
                selectedAccountProvider = server2,
            ),
            aChooseAccountProviderState(
                accountProviders = listOf(
                    server1,
                    server2,
                    server3,
                ),
                selectedAccountProvider = server2,
                loginMode = AsyncData.Loading(),
            ),
            // Add other state here
        )
}

fun aChooseAccountProviderState(
    accountProviders: List<AccountProvider> = listOf(
        anAccountProvider()
    ),
    selectedAccountProvider: AccountProvider? = null,
    loginMode: AsyncData<LoginMode> = AsyncData.Uninitialized,
    eventSink: (ChooseAccountProviderEvents) -> Unit = {},
) = ChooseAccountProviderState(
    accountProviders = accountProviders.toImmutableList(),
    selectedAccountProvider = selectedAccountProvider,
    loginMode = loginMode,
    eventSink = eventSink,
)
