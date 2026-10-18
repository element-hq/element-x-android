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
    /** Position of this test in the list shown to the user; tests run in ascending order. */
    val order: Int

    /** The current state of this test, which the UI observes to show it as idle, running, successful or failed. */
    val state: StateFlow<NotificationTroubleshootTestState>

    /**
     * Whether this test applies to the current setup, so that irrelevant tests are hidden rather than shown as failing.
     *
     * @param data the current push provider and distributor, used to decide relevance.
     */
    fun isRelevant(data: TestFilterData): Boolean = true

    /**
     * Runs the test, reporting its outcome through [state] rather than by returning or throwing.
     *
     * @param coroutineScope the scope to launch any work that must outlive the call itself.
     */
    suspend fun run(coroutineScope: CoroutineScope)

    /** Returns the test to its idle state so it can be run again. */
    suspend fun reset()

    /**
     * Attempts to fix what this test detected, for the tests that can offer it. Throws by default, so check the state's quick fix
     * flag before calling.
     *
     * @param coroutineScope the scope to launch any work that must outlive the call itself.
     * @param navigator used by the fixes that have to send the user to another screen.
     */
    suspend fun quickFix(
        coroutineScope: CoroutineScope,
        navigator: NotificationTroubleshootNavigator,
    ) {
        error("Quick fix not implemented, you need to override this method in your test")
    }
}
