/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.x

import android.app.Application
import androidx.startup.AppInitializer
import io.element.android.features.cachecleaner.api.CacheCleanerInitializer
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.x.di.AppComponent
import io.element.android.x.di.DaggerAppComponent
import io.element.android.x.info.logApplicationInfo
import io.element.android.x.initializer.CrashInitializer
import io.element.android.x.initializer.TracingInitializer

class ElementXApplication : Application(), DaggerComponentOwner {
    override val daggerComponent: AppComponent = DaggerAppComponent.factory().create(this)

    override fun onCreate() {
        super.onCreate()
        AppInitializer.getInstance(this).apply {
            initializeComponent(CrashInitializer::class.java)
            initializeComponent(TracingInitializer::class.java)
            initializeComponent(CacheCleanerInitializer::class.java)
        }
        logApplicationInfo(this)
    }
}
