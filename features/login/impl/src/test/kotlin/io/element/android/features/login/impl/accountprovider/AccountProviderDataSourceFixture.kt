/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun anAccountProviderDataSource(
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
    appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) = AccountProviderDataSource(
    enterpriseService = enterpriseService,
    appPreferencesStore = appPreferencesStore,
    coroutineScope = coroutineScope,
)
