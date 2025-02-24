/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.verification

import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.FlowId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.verification.SessionVerificationRequestDetails
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import org.matrix.rustcomponents.sdk.SessionVerificationRequestDetails as RustSessionVerificationRequestDetails
import org.matrix.rustcomponents.sdk.UserProfile as RustUserProfile

fun RustSessionVerificationRequestDetails.map() = SessionVerificationRequestDetails(
    senderProfile = senderProfile.map(),
    flowId = FlowId(flowId),
    deviceId = DeviceId(deviceId),
    firstSeenTimestamp = firstSeenTimestamp.toLong(),
)

fun RustUserProfile.map() = SessionVerificationRequestDetails.SenderProfile(
    userId = UserId(userId),
    displayName = displayName,
    avatarUrl = avatarUrl,
)

fun RustSessionVerificationRequestDetails.toVerificationRequest(currentUserId: UserId): VerificationRequest.Incoming {
    val details = map()
    return if (currentUserId == details.senderProfile.userId) {
        VerificationRequest.Incoming.OtherSession(details)
    } else {
        VerificationRequest.Incoming.User(details)
    }
}
