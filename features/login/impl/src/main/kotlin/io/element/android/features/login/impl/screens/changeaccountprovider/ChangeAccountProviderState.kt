/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.changeaccountprovider

import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.changeserver.ChangeServerState

// Do not use default value, so no member get forgotten in the presenters.
data class ChangeAccountProviderState constructor(
    val accountProviders: List<AccountProvider>,
    val changeServerState: ChangeServerState,
)
