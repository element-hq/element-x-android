/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.services.analytics.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.di.AppScope
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.ScreenTracker
import io.element.android.services.toolbox.api.systemclock.SystemClock
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultScreenTracker @Inject constructor(
    private val analyticsService: AnalyticsService,
    private val systemClock: SystemClock
) : ScreenTracker {
    @Composable
    override fun TrackScreen(
        screen: MobileScreen.ScreenName,
    ) {
        var startTime by remember { mutableLongStateOf(0L) }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    startTime = systemClock.epochMillis()
                }
                Lifecycle.Event.ON_PAUSE -> analyticsService.screen(
                    screen = MobileScreen(
                        durationMs = (systemClock.epochMillis() - startTime).toInt(),
                        screenName = screen
                    )
                )
                else -> Unit
            }
        }
    }
}
