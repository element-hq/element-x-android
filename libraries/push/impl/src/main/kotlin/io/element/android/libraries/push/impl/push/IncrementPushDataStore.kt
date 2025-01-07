/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.push.impl.store.DefaultPushDataStore
import javax.inject.Inject

interface IncrementPushDataStore {
    suspend fun incrementPushCounter()
}

@ContributesBinding(AppScope::class)
class DefaultIncrementPushDataStore @Inject constructor(
    private val defaultPushDataStore: DefaultPushDataStore
) : IncrementPushDataStore {
    override suspend fun incrementPushCounter() {
        defaultPushDataStore.incrementPushCounter()
    }
}
