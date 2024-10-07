/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class EnableNativeSlidingSyncUseCase @Inject constructor(
    private val appPreferencesStore: AppPreferencesStore,
    private val appCoroutineScope: CoroutineScope,
) {
    operator fun invoke() {
        appCoroutineScope.launch {
            appPreferencesStore.setSimplifiedSlidingSyncEnabled(true)
        }
    }
}
