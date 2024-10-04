/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.analytics

import im.vector.app.features.analytics.plan.Error
import io.element.android.services.analytics.api.AnalyticsService
import org.matrix.rustcomponents.sdk.UnableToDecryptDelegate
import org.matrix.rustcomponents.sdk.UnableToDecryptInfo
import timber.log.Timber
import uniffi.matrix_sdk_crypto.UtdCause

class UtdTracker(
    private val analyticsService: AnalyticsService,
) : UnableToDecryptDelegate {
    override fun onUtd(info: UnableToDecryptInfo) {
        Timber.d("onUtd for event ${info.eventId}, timeToDecryptMs: ${info.timeToDecryptMs}")
        val name = when (info.cause) {
            UtdCause.UNKNOWN -> Error.Name.OlmKeysNotSentError
            UtdCause.MEMBERSHIP -> Error.Name.ExpectedDueToMembership
        }
        val event = Error(
            context = null,
            // Keep cryptoModule for compatibility.
            cryptoModule = Error.CryptoModule.Rust,
            cryptoSDK = Error.CryptoSDK.Rust,
            timeToDecryptMillis = info.timeToDecryptMs?.toInt() ?: -1,
            domain = Error.Domain.E2EE,
            name = name,
        )
        analyticsService.capture(event)
    }
}
