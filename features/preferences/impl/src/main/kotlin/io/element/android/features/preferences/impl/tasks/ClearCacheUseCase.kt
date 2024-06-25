/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalCoilApi::class)

package io.element.android.features.preferences.impl.tasks

import android.content.Context
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.ftue.api.state.FtueService
import io.element.android.features.preferences.impl.DefaultCacheService
import io.element.android.features.roomlist.api.migration.MigrationScreenStore
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.api.PushService
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
    private val migrationScreenStore: MigrationScreenStore,
    private val pushService: PushService,
) : ClearCacheUseCase {
    override suspend fun invoke() = withContext(coroutineDispatchers.io) {
        // Clear Matrix cache
        matrixClient.clearCache()
        // Clear Coil cache
        Coil.imageLoader(context).let {
            it.diskCache?.clear()
            it.memoryCache?.clear()
        }
        // Clear OkHttp cache
        okHttpClient.get().cache?.delete()
        // Clear app cache
        context.cacheDir.deleteRecursively()
        // Clear some settings
        ftueService.reset()
        // Clear migration screen store
        migrationScreenStore.reset()
        // Ensure any error will be displayed again
        pushService.setIgnoreRegistrationError(matrixClient.sessionId, false)
        // Ensure the app is restarted
        defaultCacheService.onClearedCache(matrixClient.sessionId)
    }
}
