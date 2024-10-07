/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.searchaccountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.login.impl.changeserver.aChangeServerState
import io.element.android.features.login.impl.resolver.HomeserverData
import io.element.android.libraries.architecture.AsyncData

open class SearchAccountProviderStateProvider : PreviewParameterProvider<SearchAccountProviderState> {
    override val values: Sequence<SearchAccountProviderState>
        get() = sequenceOf(
            aSearchAccountProviderState(),
            aSearchAccountProviderState(userInputResult = AsyncData.Success(aHomeserverDataList())),
            // Add other state here
        )
}

fun aSearchAccountProviderState(
    userInput: String = "",
    userInputResult: AsyncData<List<HomeserverData>> = AsyncData.Uninitialized,
) = SearchAccountProviderState(
    userInput = userInput,
    userInputResult = userInputResult,
    changeServerState = aChangeServerState(),
    eventSink = {}
)

fun aHomeserverDataList(): List<HomeserverData> {
    return listOf(
        aHomeserverData(isWellknownValid = true),
        aHomeserverData(homeserverUrl = "https://no.sliding.sync", isWellknownValid = true),
        aHomeserverData(homeserverUrl = "https://invalid", isWellknownValid = false),
    )
}

fun aHomeserverData(
    homeserverUrl: String = AuthenticationConfig.MATRIX_ORG_URL,
    isWellknownValid: Boolean = true,
): HomeserverData {
    return HomeserverData(
        homeserverUrl = homeserverUrl,
        isWellknownValid = isWellknownValid,
    )
}
