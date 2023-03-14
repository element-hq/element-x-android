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

import android.os.Bundle
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

private const val SAVE_INSTANCE_KEY = "io.element.android.x.di.MatrixClientsHolder.SaveInstanceKey"

@SingleIn(AppScope::class)
class MatrixClientsHolder @Inject constructor(private val authenticationService: MatrixAuthenticationService) {

    private val sessionIdsToMatrixClient = ConcurrentHashMap<SessionId, MatrixClient>()

    fun add(matrixClient: MatrixClient) {
        sessionIdsToMatrixClient[matrixClient.sessionId] = matrixClient
    }

    fun removeAll() {
        sessionIdsToMatrixClient.clear()
    }

    fun remove(sessionId: SessionId) {
        sessionIdsToMatrixClient.remove(sessionId)
    }

    fun isEmpty(): Boolean = sessionIdsToMatrixClient.isEmpty()

    fun knowSession(sessionId: SessionId): Boolean = sessionIdsToMatrixClient.containsKey(sessionId)

    fun getOrNull(sessionId: SessionId): MatrixClient? {
        return sessionIdsToMatrixClient[sessionId]
    }

    @Suppress("DEPRECATION")
    fun restore(savedInstanceState: Bundle?) {
        if (savedInstanceState == null || sessionIdsToMatrixClient.isNotEmpty()) return
        val userIds = savedInstanceState.getSerializable(SAVE_INSTANCE_KEY) as? Array<UserId>
        if (userIds.isNullOrEmpty()) return
        // Not ideal but should only happens in case of process recreation. This ensure we restore all the active sessions before restoring the node graphs.
        runBlocking {
            userIds.forEach { userId ->
                Timber.v("Restore matrix session: $userId")
                authenticationService.restoreSession(userId)
                    .onSuccess { matrixClient ->
                        add(matrixClient)
                    }
                    .onFailure {
                        Timber.e("Fail to restore session")
                    }
            }
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        val sessionKeys = sessionIdsToMatrixClient.keys.toTypedArray()
        Timber.v("Save matrix session keys = $sessionKeys")
        outState.putSerializable(SAVE_INSTANCE_KEY, sessionKeys)
    }
}
