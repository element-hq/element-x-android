/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.api

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkRequest

interface WorkManagerRequestBuilder {
    suspend fun build(): Result<WorkManagerRequestWrapper>
}

data class WorkManagerRequestWrapper(
    val requests: List<WorkRequest>,
    val type: WorkManagerWorkerType = WorkManagerWorkerType.Default,
)

sealed interface WorkManagerWorkerType {
    data class Unique(val name: String, val policy: ExistingWorkPolicy) : WorkManagerWorkerType
    data object Default : WorkManagerWorkerType
}
