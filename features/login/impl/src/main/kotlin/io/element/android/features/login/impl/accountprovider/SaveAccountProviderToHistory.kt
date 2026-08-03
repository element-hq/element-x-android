/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import dev.zacsweers.metro.Inject
import io.element.android.libraries.preferences.api.store.AppPreferencesStore

/**
 * Persist the account provider the user just authenticated against, so that it can be offered as
 * the default (see [AccountProviderDataSource]) and as an autocomplete suggestion on the next
 * sign-in. To be called from the manual sign-in / account creation flows only, on success.
 *
 * See [AppPreferencesStore.addHomeserverToHistory].
 */
@Inject
class SaveAccountProviderToHistory(
    private val accountProviderDataSource: AccountProviderDataSource,
    private val appPreferencesStore: AppPreferencesStore,
) {
    suspend operator fun invoke() {
        appPreferencesStore.addHomeserverToHistory(accountProviderDataSource.flow.value.url)
    }
}
