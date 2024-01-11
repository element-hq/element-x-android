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

package io.element.android.features.analytics.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AnalyticsOptInPresenter @Inject constructor(
    private val buildMeta: BuildMeta,
    private val analyticsService: AnalyticsService,
) : Presenter<AnalyticsOptInState> {
    @Composable
    override fun present(): AnalyticsOptInState {
        val localCoroutineScope = rememberCoroutineScope()

        fun handleEvents(event: AnalyticsOptInEvents) {
            when (event) {
                is AnalyticsOptInEvents.EnableAnalytics -> localCoroutineScope.setIsEnabled(event.isEnabled)
            }
            localCoroutineScope.launch {
                analyticsService.setDidAskUserConsent()
            }
        }

        return AnalyticsOptInState(
            applicationName = buildMeta.applicationName,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.setIsEnabled(enabled: Boolean) = launch {
        analyticsService.setUserConsent(enabled)
    }
}
