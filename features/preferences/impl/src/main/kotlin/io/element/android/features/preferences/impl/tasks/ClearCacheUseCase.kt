/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.tasks

import android.content.Context
import coil3.SingletonImageLoader
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.ftue.api.state.FtueService
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.preferences.impl.DefaultCacheService
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.api.PushService
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider

interface ClearCacheUseCase {
    suspend operator fun invoke()
}

@ContributesBinding(SessionScope::class)
class DefaultClearCacheUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val matrixClient: MatrixClient,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val defaultCacheService: DefaultCacheService,
    private val okHttpClient: Provider<OkHttpClient>,
    private val ftueService: FtueService,
    private val pushService: PushService,
    private val seenInvitesStore: SeenInvitesStore,
    private val activeRoomsHolder: ActiveRoomsHolder,
) : ClearCacheUseCase {
    override suspend fun invoke() = withContext(coroutineDispatchers.io) {
        // Active rooms should be disposed of before clearing the cache
        activeRoomsHolder.clear(matrixClient.sessionId)
        // Clear Matrix cache
        matrixClient.clearCache()
        // Clear Coil cache
        SingletonImageLoader.get(context).let {
            it.diskCache?.clear()
            it.memoryCache?.clear()
        }
        // Clear OkHttp cache
        okHttpClient.get().cache?.delete()
        // Clear app cache
        context.cacheDir.deleteRecursively()
        // Clear some settings
        ftueService.reset()
        seenInvitesStore.clear()
        // Ensure any error will be displayed again
        pushService.setIgnoreRegistrationError(matrixClient.sessionId, false)
        // Ensure the app is restarted
        defaultCacheService.onClearedCache(matrixClient.sessionId)
    }
}
