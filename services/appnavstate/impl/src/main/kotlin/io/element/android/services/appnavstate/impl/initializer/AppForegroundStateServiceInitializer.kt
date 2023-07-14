/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.services.appnavstate.impl.initializer

import android.content.Context
import androidx.lifecycle.ProcessLifecycleInitializer
import androidx.startup.Initializer
import io.element.android.services.appnavstate.api.AppForegroundStateService
import io.element.android.services.appnavstate.impl.DefaultAppForegroundStateService

class AppForegroundStateServiceInitializer : Initializer<AppForegroundStateService> {
    override fun create(context: Context): AppForegroundStateService {
        return DefaultAppForegroundStateService()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf(
        ProcessLifecycleInitializer::class.java
    )
}
