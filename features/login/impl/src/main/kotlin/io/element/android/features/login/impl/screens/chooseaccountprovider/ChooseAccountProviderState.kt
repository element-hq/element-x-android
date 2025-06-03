/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncData

// Do not use default value, so no member get forgotten in the presenters.
data class ChooseAccountProviderState(
    val accountProviders: List<AccountProvider>,
    val selectedAccountProvider: AccountProvider?,
    val loginMode: AsyncData<LoginMode>,
    val eventSink: (ChooseAccountProviderEvents) -> Unit,
) {
    val submitEnabled: Boolean
        get() = selectedAccountProvider != null && (loginMode is AsyncData.Uninitialized || loginMode is AsyncData.Loading)
}
