/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.changeaccountprovider

import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.changeserver.ChangeServerState

// Do not use default value, so no member get forgotten in the presenters.
data class ChangeAccountProviderState(
    val accountProviders: List<AccountProvider>,
    val canSearchForAccountProviders: Boolean,
    val changeServerState: ChangeServerState,
)
