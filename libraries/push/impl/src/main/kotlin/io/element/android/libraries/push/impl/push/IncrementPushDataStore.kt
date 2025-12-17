/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.push.impl.store.DefaultPushDataStore

interface IncrementPushDataStore {
    suspend fun incrementPushCounter()
}

@ContributesBinding(AppScope::class)
class DefaultIncrementPushDataStore(
    private val defaultPushDataStore: DefaultPushDataStore
) : IncrementPushDataStore {
    override suspend fun incrementPushCounter() {
        defaultPushDataStore.incrementPushCounter()
    }
}
