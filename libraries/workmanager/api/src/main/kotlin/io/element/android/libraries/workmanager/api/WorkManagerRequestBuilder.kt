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

/**
 * A base class that can be customized to [build] work requests to schedule in `WorkManager`.
 */
interface WorkManagerRequestBuilder {
    /**
     * Builds a work request wrapper using the provided data.
     */
    suspend fun build(): Result<List<WorkManagerRequestWrapper>>
}

/**
 * A wrapper that allows us to avoid using Android APIs directly when scheduling workers.
 */
data class WorkManagerRequestWrapper(
    val request: WorkRequest,
    val type: WorkManagerWorkerType = WorkManagerWorkerType.Default,
)

/**
 * The type of worker to use when scheduling the task.
 */
sealed interface WorkManagerWorkerType {
    /**
     * This allows a single worker instance with the [name] id to run at the same time. Its [policy] can be customized.
     */
    data class Unique(val name: String, val policy: ExistingWorkPolicy) : WorkManagerWorkerType

    /**
     * The default worker type, with no custom rules.
     */
    data object Default : WorkManagerWorkerType
}
