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

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Error
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.services.analytics.test.FakeAnalyticsService
import org.junit.Test
import org.matrix.rustcomponents.sdk.UnableToDecryptInfo

class UtdTrackerTest {
    @Test
    fun `when onUtd is called with null timeToDecryptMs, the expected analytics Event is sent`() {
        val fakeAnalyticsService = FakeAnalyticsService()
        val sut = UtdTracker(fakeAnalyticsService)
        sut.onUtd(UnableToDecryptInfo(eventId = AN_EVENT_ID.value, timeToDecryptMs = null))
        assertThat(fakeAnalyticsService.capturedEvents).containsExactly(
            Error(
                context = null,
                cryptoModule = Error.CryptoModule.Rust,
                cryptoSDK = Error.CryptoSDK.Rust,
                timeToDecryptMillis = -1,
                domain = Error.Domain.E2EE,
                name = Error.Name.OlmKeysNotSentError
            )
        )
        assertThat(fakeAnalyticsService.screenEvents).isEmpty()
        assertThat(fakeAnalyticsService.trackedErrors).isEmpty()
    }

    @Test
    fun `when onUtd is called with timeToDecryptMs, the expected analytics Event is sent`() {
        val fakeAnalyticsService = FakeAnalyticsService()
        val sut = UtdTracker(fakeAnalyticsService)
        sut.onUtd(UnableToDecryptInfo(eventId = AN_EVENT_ID.value, timeToDecryptMs = 123.toULong()))
        assertThat(fakeAnalyticsService.capturedEvents).containsExactly(
            Error(
                context = null,
                cryptoModule = Error.CryptoModule.Rust,
                cryptoSDK = Error.CryptoSDK.Rust,
                timeToDecryptMillis = 123,
                domain = Error.Domain.E2EE,
                name = Error.Name.OlmKeysNotSentError
            )
        )
        assertThat(fakeAnalyticsService.screenEvents).isEmpty()
        assertThat(fakeAnalyticsService.trackedErrors).isEmpty()
    }
}
