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

package io.element.android.libraries.matrix.impl.analytics

import im.vector.app.features.analytics.plan.Error
import io.element.android.services.analytics.api.AnalyticsService
import org.matrix.rustcomponents.sdk.UnableToDecryptDelegate
import org.matrix.rustcomponents.sdk.UnableToDecryptInfo
import timber.log.Timber
import javax.inject.Inject

class UtdTracker @Inject constructor(
    private val analyticsService: AnalyticsService,
) : UnableToDecryptDelegate {
    override fun onUtd(info: UnableToDecryptInfo) {
        Timber.d("onUtd for event ${info.eventId}, timeToDecryptMs: ${info.timeToDecryptMs}")
        // TODO info will contain more information in the future, so that the app can report more precise data to the analytics.
        val event = Error(
            context = null,
            cryptoModule = Error.CryptoModule.Rust,
            domain = Error.Domain.E2EE,
            // TODO get a more specific error name from `info`
            name = Error.Name.OlmKeysNotSentError,
        )
        analyticsService.capture(event)
    }
}
