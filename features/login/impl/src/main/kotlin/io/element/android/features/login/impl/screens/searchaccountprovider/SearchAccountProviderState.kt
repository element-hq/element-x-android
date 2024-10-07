/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.searchaccountprovider

import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.resolver.HomeserverData
import io.element.android.libraries.architecture.AsyncData

// Do not use default value, so no member get forgotten in the presenters.
data class SearchAccountProviderState(
    val userInput: String,
    val userInputResult: AsyncData<List<HomeserverData>>,
    val changeServerState: ChangeServerState,
    val eventSink: (SearchAccountProviderEvents) -> Unit
)
