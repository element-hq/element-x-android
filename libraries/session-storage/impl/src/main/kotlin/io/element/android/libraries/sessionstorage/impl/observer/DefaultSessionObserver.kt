/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.impl.observer

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.sessionstorage.api.toUserListFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArraySet

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultSessionObserver(
    private val sessionStore: SessionStore,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
) : SessionObserver {
    // Keep only the userId
    private var currentUsers: Set<String>? = null

    init {
        observeDatabase()
    }

    private val listeners = CopyOnWriteArraySet<SessionListener>()
    override fun addListener(listener: SessionListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SessionListener) {
        listeners.remove(listener)
    }

    private fun observeDatabase() {
        coroutineScope.launch {
            withContext(dispatchers.io) {
                sessionStore.sessionsFlow()
                    .toUserListFlow()
                    .map { it.toSet() }
                    .onEach { newUserSet ->
                        val currentUserSet = currentUsers
                        if (currentUserSet != null) {
                            // Compute diff
                            // Removed user
                            val removedUsers = currentUserSet - newUserSet
                            val wasLastSession = newUserSet.isEmpty()
                            removedUsers.forEach { removedUser ->
                                listeners.onEach { listener ->
                                    listener.onSessionDeleted(removedUser, wasLastSession)
                                }
                            }
                            // Added user
                            val addedUsers = newUserSet - currentUserSet
                            addedUsers.forEach { addedUser ->
                                listeners.onEach { listener ->
                                    listener.onSessionCreated(addedUser)
                                }
                            }
                        }

                        currentUsers = newUserSet
                    }
                    .collect()
            }
        }
    }
}
