/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.api.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlin.reflect.KClass

@ContributesBinding(AppScope::class)
class MetroWorkerFactory(
    val workerProviders: Map<KClass<out ListenableWorker>, WorkerInstanceFactory<*>>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
      return workerProviders[Class.forName(workerClassName).kotlin]?.create(workerParameters)
    }

    interface WorkerInstanceFactory<T : ListenableWorker> {
      fun create(params: WorkerParameters): T
    }
}
