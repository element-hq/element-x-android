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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

private const val SAVE_INSTANCE_KEY = "io.element.android.x.di.MatrixClientsHolder.SaveInstanceKey"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class MatrixClientsHolder @Inject constructor(private val authenticationService: MatrixAuthenticationService) : MatrixClientProvider {

    private val sessionIdsToMatrixClient = ConcurrentHashMap<SessionId, MatrixClient>()
    private val restoreMutex = Mutex()

    fun removeAll() {
        sessionIdsToMatrixClient.clear()
    }

    fun remove(sessionId: SessionId) {
        sessionIdsToMatrixClient.remove(sessionId)
    }

    override fun getOrNull(sessionId: SessionId): MatrixClient? {
        return sessionIdsToMatrixClient[sessionId]
    }

    override suspend fun getOrRestore(sessionId: SessionId): Result<MatrixClient> {
        return restoreMutex.withLock {
            when (val matrixClient = getOrNull(sessionId)) {
                null -> restore(sessionId)
                else -> Result.success(matrixClient)
            }
        }
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
                restore(sessionId)
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
                sessionIdsToMatrixClient[matrixClient.sessionId] = matrixClient
            }
            .onFailure {
                Timber.e(it, "Fail to restore session")
            }
    }
}
