/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.CoroutineScope

@Module
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
