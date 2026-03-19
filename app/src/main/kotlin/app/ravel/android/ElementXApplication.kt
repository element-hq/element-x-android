/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package app.ravel.android

import android.app.Application
import androidx.startup.AppInitializer
import androidx.work.Configuration
import app.ravel.android.di.AppGraph
import app.ravel.android.info.logApplicationInfo
import app.ravel.android.initializer.CacheCleanerInitializer
import app.ravel.android.initializer.CrashInitializer
import app.ravel.android.initializer.PlatformInitializer
import dev.zacsweers.metro.createGraphFactory
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory

class ElementXApplication : Application(), DependencyInjectionGraphOwner, Configuration.Provider {
    override val graph: AppGraph = createGraphFactory<AppGraph.Factory>().create(this)

    override val workManagerConfiguration: Configuration = Configuration.Builder()
        .setWorkerFactory(MetroWorkerFactory(graph.workerProviders))
        .build()

    override fun onCreate() {
        super.onCreate()
        AppInitializer.getInstance(this).apply {
            initializeComponent(CrashInitializer::class.java)
            initializeComponent(PlatformInitializer::class.java)
            initializeComponent(CacheCleanerInitializer::class.java)
        }

        logApplicationInfo(this)
    }
}
