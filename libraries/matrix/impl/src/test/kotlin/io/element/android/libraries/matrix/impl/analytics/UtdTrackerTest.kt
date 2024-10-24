/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.analytics

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Error
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.services.analytics.test.FakeAnalyticsService
import org.junit.Test
import org.matrix.rustcomponents.sdk.UnableToDecryptInfo
import uniffi.matrix_sdk_crypto.UtdCause

class UtdTrackerTest {
    @Test
    fun `when onUtd is called with null timeToDecryptMs, the expected analytics Event is sent`() {
        val fakeAnalyticsService = FakeAnalyticsService()
        val sut = UtdTracker(fakeAnalyticsService)
        sut.onUtd(
            UnableToDecryptInfo(
                eventId = AN_EVENT_ID.value,
                timeToDecryptMs = null,
                cause = UtdCause.UNKNOWN,
            )
        )
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
        sut.onUtd(
            UnableToDecryptInfo(
                eventId = AN_EVENT_ID.value,
                timeToDecryptMs = 123.toULong(),
                cause = UtdCause.UNKNOWN,
            )
        )
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

    @Test
    fun `when onUtd is called with membership cause, the expected analytics Event is sent`() {
        val fakeAnalyticsService = FakeAnalyticsService()
        val sut = UtdTracker(fakeAnalyticsService)
        sut.onUtd(
            UnableToDecryptInfo(
                eventId = AN_EVENT_ID.value,
                timeToDecryptMs = 123.toULong(),
                cause = UtdCause.SENT_BEFORE_WE_JOINED,
            )
        )
        assertThat(fakeAnalyticsService.capturedEvents).containsExactly(
            Error(
                context = null,
                cryptoModule = Error.CryptoModule.Rust,
                cryptoSDK = Error.CryptoSDK.Rust,
                timeToDecryptMillis = 123,
                domain = Error.Domain.E2EE,
                name = Error.Name.ExpectedDueToMembership
            )
        )
        assertThat(fakeAnalyticsService.screenEvents).isEmpty()
        assertThat(fakeAnalyticsService.trackedErrors).isEmpty()
    }

    @Test
    fun `when onUtd is called with insecure cause, the expected analytics Event is sent`() {
        val fakeAnalyticsService = FakeAnalyticsService()
        val sut = UtdTracker(fakeAnalyticsService)
        sut.onUtd(
            UnableToDecryptInfo(
                eventId = AN_EVENT_ID.value,
                timeToDecryptMs = 123.toULong(),
                cause = UtdCause.UNSIGNED_DEVICE,
            )
        )
        assertThat(fakeAnalyticsService.capturedEvents).containsExactly(
            Error(
                context = null,
                cryptoModule = Error.CryptoModule.Rust,
                cryptoSDK = Error.CryptoSDK.Rust,
                timeToDecryptMillis = 123,
                domain = Error.Domain.E2EE,
                name = Error.Name.ExpectedSentByInsecureDevice
            )
        )
    }

    @Test
    fun `when onUtd is called with verification violation cause, the expected analytics Event is sent`() {
        val fakeAnalyticsService = FakeAnalyticsService()
        val sut = UtdTracker(fakeAnalyticsService)
        sut.onUtd(
            UnableToDecryptInfo(
                eventId = AN_EVENT_ID.value,
                timeToDecryptMs = 123.toULong(),
                cause = UtdCause.VERIFICATION_VIOLATION,
            )
        )
        assertThat(fakeAnalyticsService.capturedEvents).containsExactly(
            Error(
                context = null,
                cryptoModule = Error.CryptoModule.Rust,
                cryptoSDK = Error.CryptoSDK.Rust,
                timeToDecryptMillis = 123,
                domain = Error.Domain.E2EE,
                name = Error.Name.ExpectedVerificationViolation
            )
        )
    }
}
