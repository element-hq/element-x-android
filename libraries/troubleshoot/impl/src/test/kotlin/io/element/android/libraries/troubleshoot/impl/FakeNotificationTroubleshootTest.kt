/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
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

    override suspend fun reset() {
        updateState(
            name = defaultName,
            description = defaultDescription,
            status = firstStatus,
        )
        resetAction()?.let {
            _state.emit(it)
        }
    }

    override suspend fun quickFix(
        coroutineScope: CoroutineScope,
        navigator: NotificationTroubleshootNavigator,
    ) {
        updateState(NotificationTroubleshootTestState.Status.InProgress)
        quickFixAction()?.let {
            _state.emit(it)
        }
    }

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
}
