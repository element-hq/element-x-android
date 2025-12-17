/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.searchaccountprovider

import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.resolver.HomeserverData
import io.element.android.libraries.architecture.AsyncData

data class SearchAccountProviderState(
    val userInput: String,
    val userInputResult: AsyncData<List<HomeserverData>>,
    val changeServerState: ChangeServerState,
    val eventSink: (SearchAccountProviderEvents) -> Unit
)
