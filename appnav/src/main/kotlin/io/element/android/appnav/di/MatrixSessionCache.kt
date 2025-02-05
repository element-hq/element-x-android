/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.di

import com.bumble.appyx.core.state.MutableSavedStateMap
import com.bumble.appyx.core.state.SavedStateMap
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.services.appnavstate.api.SyncOrchestrator
import io.element.android.services.appnavstate.api.SyncOrchestratorProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

private const val SAVE_INSTANCE_KEY = "io.element.android.x.di.MatrixClientsHolder.SaveInstanceKey"

/**
 * In-memory cache for logged in Matrix sessions.
 *
 * This component contains both the [MatrixClient] and the [SyncOrchestrator] for each session.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, boundType = MatrixClientProvider::class)
@ContributesBinding(AppScope::class, boundType = SyncOrchestratorProvider::class)
class MatrixSessionCache @Inject constructor(
    private val authenticationService: MatrixAuthenticationService,
    private val syncOrchestratorFactory: DefaultSyncOrchestrator.Factory,
) : MatrixClientProvider, SyncOrchestratorProvider {
    private val sessionIdsToMatrixClient = ConcurrentHashMap<SessionId, InMemoryMatrixSession>()
    private val restoreMutex = Mutex()

    init {
        authenticationService.listenToNewMatrixClients { matrixClient ->
            sessionIdsToMatrixClient[matrixClient.sessionId] = InMemoryMatrixSession(
                matrixClient = matrixClient,
                syncOrchestrator = syncOrchestratorFactory.create(matrixClient)
            )
        }
    }

    fun removeAll() {
        sessionIdsToMatrixClient.clear()
    }

    fun remove(sessionId: SessionId) {
        sessionIdsToMatrixClient.remove(sessionId)
    }

    override fun getOrNull(sessionId: SessionId): MatrixClient? {
        return sessionIdsToMatrixClient[sessionId]?.matrixClient
    }

    override suspend fun getOrRestore(sessionId: SessionId): Result<MatrixClient> {
        return restoreMutex.withLock {
            when (val cached = getOrNull(sessionId)) {
                null -> restore(sessionId)
                else -> Result.success(cached)
            }
        }
    }

    override fun getSyncOrchestrator(sessionId: SessionId): SyncOrchestrator? {
        return sessionIdsToMatrixClient[sessionId]?.syncOrchestrator
    }

    @Suppress("UNCHECKED_CAST")
    fun restoreWithSavedState(state: SavedStateMap?) {
        Timber.d("Restore state")
        if (state == null || sessionIdsToMatrixClient.isNotEmpty()) {
            Timber.w("Restore with non-empty map")
            return
        }
        val sessionIds = state[SAVE_INSTANCE_KEY] as? Array<SessionId>
        Timber.d("Restore matrix session keys = ${sessionIds?.map { it.value }}")
        if (sessionIds.isNullOrEmpty()) return
        // Not ideal but should only happens in case of process recreation. This ensure we restore all the active sessions before restoring the node graphs.
        runBlocking {
            sessionIds.forEach { sessionId ->
                getOrRestore(sessionId)
            }
        }
    }

    fun saveIntoSavedState(state: MutableSavedStateMap) {
        val sessionKeys = sessionIdsToMatrixClient.keys.toTypedArray()
        Timber.d("Save matrix session keys = ${sessionKeys.map { it.value }}")
        state[SAVE_INSTANCE_KEY] = sessionKeys
    }

    private suspend fun restore(sessionId: SessionId): Result<MatrixClient> {
        Timber.d("Restore matrix session: $sessionId")
        return authenticationService.restoreSession(sessionId)
            .onSuccess { matrixClient ->
                sessionIdsToMatrixClient[matrixClient.sessionId] = InMemoryMatrixSession(
                    matrixClient = matrixClient,
                    syncOrchestrator = syncOrchestratorFactory.create(matrixClient)
                )
            }
            .onFailure {
                Timber.e(it, "Fail to restore session")
            }
    }
}

private data class InMemoryMatrixSession(
    val matrixClient: MatrixClient,
    val syncOrchestrator: SyncOrchestrator,
)
