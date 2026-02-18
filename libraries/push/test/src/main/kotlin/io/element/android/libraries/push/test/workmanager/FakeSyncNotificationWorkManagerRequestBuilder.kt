/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.workmanager

import androidx.work.WorkRequest
import io.element.android.libraries.push.api.workmanager.SyncNotificationWorkManagerRequestBuilder

class FakeSyncNotificationWorkManagerRequestBuilder(
    private val buildLambda: () -> Result<List<WorkRequest>> = { Result.success(emptyList()) },
) : SyncNotificationWorkManagerRequestBuilder {
    override suspend fun build(): Result<List<WorkRequest>> = buildLambda()
}
