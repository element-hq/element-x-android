/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
