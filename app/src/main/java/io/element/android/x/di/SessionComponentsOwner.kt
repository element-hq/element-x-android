/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.di

import android.content.Context
import io.element.android.x.architecture.bindings
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.core.SessionId
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@SingleIn(AppScope::class)
class SessionComponentsOwner @Inject constructor(@ApplicationContext private val context: Context) {

    private val sessionComponents = ConcurrentHashMap<SessionId, SessionComponent>()
    var activeSessionComponent: SessionComponent? = null
        private set

    fun setActive(sessionId: SessionId) {
        val sessionComponent = sessionComponents[sessionId]
        if (activeSessionComponent != sessionComponent) {
            activeSessionComponent = sessionComponent
        }
    }

    fun create(matrixClient: MatrixClient) {
        val sessionId = matrixClient.sessionId
        val sessionComponent =
            context.bindings<SessionComponent.ParentBindings>().sessionComponentBuilder()
                .client(matrixClient).build()
        sessionComponents[sessionId] = sessionComponent
        setActive(sessionId)
    }

    fun releaseActiveSession() {
        activeSessionComponent?.also {
            release(it.matrixClient().sessionId)
        }
    }

    fun release(sessionId: SessionId) {
        val sessionComponent = sessionComponents.remove(sessionId)
        if (activeSessionComponent == sessionComponent) {
            activeSessionComponent = null
        }
    }
}
