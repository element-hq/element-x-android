/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.api.test

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
                NotificationTroubleshootTestState.Status.Failure(hasQuickFix = hasQuickFix)
            }
        )
    }

    companion object {
        const val SHORT_DELAY = 300L
        const val LONG_DELAY = 500L
    }
}
