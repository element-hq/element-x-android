/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.accountprovider.anAccountProviderManaged
import io.element.android.features.login.impl.login.LoginModeState
import io.element.android.features.login.impl.login.aLoginModeState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.accountprovider.AccountProvider
import kotlinx.collections.immutable.toImmutableList

open class ChooseAccountProviderStatePreviewParam : PreviewParameterProvider<ChooseAccountProviderState> {
    private val server1 = anAccountProviderManaged(
        baseUrl = "https://server1.io",
    )
    private val server2 = anAccountProviderManaged(
        baseUrl = "https://server2.io",
    )
    private val server3 = anAccountProviderManaged(
        baseUrl = "https://server3.io",
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
                loginModeState = aLoginModeState(loginMode = AsyncData.Loading()),
            ),
        )
}

fun aChooseAccountProviderState(
    accountProviders: List<AccountProvider> = listOf(
        anAccountProviderManaged()
    ),
    selectedAccountProvider: AccountProvider? = null,
    loginModeState: LoginModeState = aLoginModeState(),
    eventSink: (ChooseAccountProviderEvent) -> Unit = {},
) = ChooseAccountProviderState(
    accountProviders = accountProviders.toImmutableList(),
    selectedAccountProvider = selectedAccountProvider,
    loginModeState = loginModeState,
    eventSink = eventSink,
)
