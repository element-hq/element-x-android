/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import coil3.ImageLoader
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import javax.inject.Inject

interface ImageLoaderHolder {
    fun get(client: MatrixClient): ImageLoader
    fun remove(sessionId: SessionId)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultImageLoaderHolder @Inject constructor(
    private val loggedInImageLoaderFactory: LoggedInImageLoaderFactory,
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
                loggedInImageLoaderFactory
                    .newImageLoader(client)
            }
        }
    }

    override fun remove(sessionId: SessionId) {
        synchronized(map) {
            map.remove(sessionId)
        }
    }
}
