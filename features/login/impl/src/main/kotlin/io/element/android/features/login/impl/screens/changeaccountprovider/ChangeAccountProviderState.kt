/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.changeaccountprovider

import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.changeserver.ChangeServerState
import kotlinx.collections.immutable.ImmutableList

data class ChangeAccountProviderState(
    val accountProviders: ImmutableList<AccountProvider>,
    val canSearchForAccountProviders: Boolean,
    val changeServerState: ChangeServerState,
)
