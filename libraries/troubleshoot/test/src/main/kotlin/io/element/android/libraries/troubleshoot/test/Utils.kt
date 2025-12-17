/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("UnusedImports")

package io.element.android.libraries.troubleshoot.test

import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

context(testScope: TestScope)
suspend fun NotificationTroubleshootTest.runAndTestState(
    validate: suspend TurbineTestContext<NotificationTroubleshootTestState>.() -> Unit,
) {
    testScope.backgroundScope.launch {
        run(this)
    }
    state.test(validate = validate)
}
