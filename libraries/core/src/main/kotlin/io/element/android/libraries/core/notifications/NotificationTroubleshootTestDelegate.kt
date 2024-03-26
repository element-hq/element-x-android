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

package io.element.android.libraries.core.notifications

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A NotificationTroubleshootTest delegate, with common pattern for running and resetting.
 */
class NotificationTroubleshootTestDelegate(
    private val defaultName: String,
    private val defaultDescription: String,
    private val visibleWhenIdle: Boolean = true,
    private val hasQuickFix: Boolean = false,
    private val fakeDelay: Long = 0L,
) {
    private val _state: MutableStateFlow<NotificationTroubleshootTestState> = MutableStateFlow(
        NotificationTroubleshootTestState(
            name = defaultName,
            description = defaultDescription,
            status = NotificationTroubleshootTestState.Status.Idle(visibleWhenIdle),
        )
    )

    val state: StateFlow<NotificationTroubleshootTestState> = _state.asStateFlow()

    suspend fun updateState(
        status: NotificationTroubleshootTestState.Status,
        name: String = defaultName,
        description: String = defaultDescription,
    ) {
        _state.emit(
            NotificationTroubleshootTestState(
                name = name,
                description = description,
                status = status,
            )
        )
    }

    suspend fun reset() {
        updateState(NotificationTroubleshootTestState.Status.Idle(visibleWhenIdle))
    }

    suspend fun start() {
        updateState(NotificationTroubleshootTestState.Status.InProgress)
        delay(fakeDelay)
    }

    suspend fun done(isSuccess: Boolean = true) {
        updateState(
            if (isSuccess) {
                NotificationTroubleshootTestState.Status.Success
            } else {
                NotificationTroubleshootTestState.Status.Failure(hasQuickFix)
            }
        )
    }

    companion object {
        const val SHORT_DELAY = 300L
        const val LONG_DELAY = 500L
    }
}
