/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import io.element.android.tests.testutils.simulateLongTask

class FakeFirebaseTroubleshooter(
    private val troubleShootResult: () -> Result<Unit> = { Result.success(Unit) }
) : FirebaseTroubleshooter {
    override suspend fun troubleshoot(): Result<Unit> = simulateLongTask {
        troubleShootResult()
    }
}
