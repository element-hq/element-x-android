/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultSeenInvitesStoreFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionObserver: SessionObserver,
) : SeenInvitesStoreFactory {
    // We can have only one class accessing a single data store, so keep a cache of them.
    private val cache = ConcurrentHashMap<SessionId, SeenInvitesStore>()

    override fun getOrCreate(
        sessionId: SessionId,
        sessionCoroutineScope: CoroutineScope,
    ): SeenInvitesStore {
        return cache.getOrPut(sessionId) {
            DefaultSeenInvitesStore(
                context = context,
                sessionId = sessionId,
                sessionCoroutineScope = sessionCoroutineScope,
                sessionObserver = sessionObserver,
            )
        }
    }
}
