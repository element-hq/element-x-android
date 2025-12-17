/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.api.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface NotificationTroubleshootTest {
    val order: Int
    val state: StateFlow<NotificationTroubleshootTestState>
    fun isRelevant(data: TestFilterData): Boolean = true
    suspend fun run(coroutineScope: CoroutineScope)
    suspend fun reset()
    suspend fun quickFix(
        coroutineScope: CoroutineScope,
        navigator: NotificationTroubleshootNavigator,
    ) {
        error("Quick fix not implemented, you need to override this method in your test")
    }
}
