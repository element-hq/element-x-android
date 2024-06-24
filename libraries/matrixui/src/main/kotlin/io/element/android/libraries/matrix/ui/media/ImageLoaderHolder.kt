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

package io.element.android.libraries.matrix.ui.media

import android.content.Context
import coil.ImageLoader
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider

interface ImageLoaderHolder {
    fun get(client: MatrixClient): ImageLoader
    fun remove(sessionId: SessionId)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultImageLoaderHolder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: Provider<OkHttpClient>,
    private val sessionObserver: SessionObserver,
) : ImageLoaderHolder {
    private val map = mutableMapOf<SessionId, ImageLoader>()

    init {
        observeSessions()
    }

    private fun observeSessions() {
        sessionObserver.addListener(object : SessionListener {
            override suspend fun onSessionCreated(userId: String) = Unit

            override suspend fun onSessionDeleted(userId: String) {
                remove(SessionId(userId))
            }
        })
    }

    override fun get(client: MatrixClient): ImageLoader {
        return synchronized(map) {
            map.getOrPut(client.sessionId) {
                LoggedInImageLoaderFactory(
                    context = context,
                    matrixClient = client,
                    okHttpClient = okHttpClient,
                ).newImageLoader()
            }
        }
    }

    override fun remove(sessionId: SessionId) {
        synchronized(map) {
            map.remove(sessionId)
        }
    }
}
