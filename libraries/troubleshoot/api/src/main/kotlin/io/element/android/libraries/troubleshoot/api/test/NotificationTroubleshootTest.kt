/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.api.test

import io.element.android.libraries.di.SessionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * A test to troubleshoot notifications issues.
 * Each test has a state that can be observed to update the UI accordingly.
 *
 * **IMPORTANT**: classes implementing this should be scoped to [SessionScope], otherwise Metro complains about these not being used:
 * the component they're injected into is bound to [SessionScope] and so should these (https://github.com/ZacSweers/metro/issues/1932).
 */
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
