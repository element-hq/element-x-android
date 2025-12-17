/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
            UtdCause.SENT_BEFORE_WE_JOINED -> Error.Name.ExpectedDueToMembership
            UtdCause.VERIFICATION_VIOLATION -> Error.Name.ExpectedVerificationViolation
            UtdCause.UNSIGNED_DEVICE,
            UtdCause.UNKNOWN_DEVICE -> {
                Error.Name.ExpectedSentByInsecureDevice
            }
            UtdCause.HISTORICAL_MESSAGE_AND_BACKUP_IS_DISABLED,
            UtdCause.HISTORICAL_MESSAGE_AND_DEVICE_IS_UNVERIFIED,
                -> Error.Name.HistoricalMessage
            UtdCause.WITHHELD_FOR_UNVERIFIED_OR_INSECURE_DEVICE -> Error.Name.RoomKeysWithheldForUnverifiedDevice
            UtdCause.WITHHELD_BY_SENDER -> Error.Name.OlmKeysNotSentError
        }
        val event = Error(
            context = null,
            // Keep cryptoModule for compatibility.
            cryptoModule = Error.CryptoModule.Rust,
            cryptoSDK = Error.CryptoSDK.Rust,
            timeToDecryptMillis = info.timeToDecryptMs?.toInt() ?: -1,
            domain = Error.Domain.E2EE,
            name = name,
            eventLocalAgeMillis = info.eventLocalAgeMillis.toInt(),
            userTrustsOwnIdentity = info.userTrustsOwnIdentity,
            isFederated = info.ownHomeserver != info.senderHomeserver,
            isMatrixDotOrg = info.ownHomeserver == "matrix.org",
        )
        analyticsService.capture(event)
    }
}
