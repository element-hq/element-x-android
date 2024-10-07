/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import io.element.android.features.login.impl.util.defaultAccountProvider
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@SingleIn(AppScope::class)
class AccountProviderDataSource @Inject constructor() {
    private val accountProvider: MutableStateFlow<AccountProvider> = MutableStateFlow(
        defaultAccountProvider
    )

    fun flow(): StateFlow<AccountProvider> {
        return accountProvider.asStateFlow()
    }

    fun reset() {
        accountProvider.tryEmit(defaultAccountProvider)
    }

    fun userSelection(data: AccountProvider) {
        accountProvider.tryEmit(data)
    }
}
