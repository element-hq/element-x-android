/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.preferences.impl.notifications.troubleshoot

import io.element.android.libraries.core.notifications.NotificationTroubleshootTest
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNotificationTroubleshootTest(
    override val order: Int = 0,
    private val defaultName: String = "test name",
    private val defaultDescription: String = "test description",
    private val firstStatus: NotificationTroubleshootTestState.Status = NotificationTroubleshootTestState.Status.Idle(visible = true),
    private val runAction: () -> NotificationTroubleshootTestState? = { null },
    private val resetAction: () -> NotificationTroubleshootTestState? = { null },
    private val quickFixAction: () -> NotificationTroubleshootTestState? = { null },
) : NotificationTroubleshootTest {
    private val _state = MutableStateFlow(
        NotificationTroubleshootTestState(
            name = defaultName,
            description = defaultDescription,
            status = firstStatus
        )
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = _state.asStateFlow()

    override suspend fun run(coroutineScope: CoroutineScope) {
        updateState(NotificationTroubleshootTestState.Status.InProgress)
        runAction()?.let {
            _state.tryEmit(it)
        }
    }

    override fun reset() {
        updateState(
            name = defaultName,
            description = defaultDescription,
            status = firstStatus,
        )
        resetAction()?.let {
            _state.tryEmit(it)
        }
    }

    override suspend fun quickFix(coroutineScope: CoroutineScope) {
        updateState(NotificationTroubleshootTestState.Status.InProgress)
        quickFixAction()?.let {
            _state.tryEmit(it)
        }
    }

    fun updateState(
        status: NotificationTroubleshootTestState.Status,
        name: String = defaultName,
        description: String = defaultDescription,
    ) {
        _state.tryEmit(
            NotificationTroubleshootTestState(
                name = name,
                description = description,
                status = status,
            )
        )
    }
}
