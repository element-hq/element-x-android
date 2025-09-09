/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(SessionScope::class)
object SessionPreferencesModule {
    @Provides
    fun providesSessionPreferencesStore(
        defaultSessionPreferencesStoreFactory: DefaultSessionPreferencesStoreFactory,
        currentSessionIdHolder: CurrentSessionIdHolder,
        @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
    ): SessionPreferencesStore {
        return defaultSessionPreferencesStoreFactory
            .get(currentSessionIdHolder.current, sessionCoroutineScope)
    }
}
