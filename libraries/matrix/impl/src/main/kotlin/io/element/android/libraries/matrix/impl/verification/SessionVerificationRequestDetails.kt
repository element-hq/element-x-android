/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.verification

import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.FlowId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.verification.SessionVerificationRequestDetails
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import io.element.android.libraries.matrix.impl.mapper.map
import org.matrix.rustcomponents.sdk.SessionVerificationRequestDetails as RustSessionVerificationRequestDetails

fun RustSessionVerificationRequestDetails.map() = SessionVerificationRequestDetails(
    senderProfile = senderProfile.map(),
    flowId = FlowId(flowId),
    deviceId = DeviceId(deviceId),
    deviceDisplayName = deviceDisplayName,
    firstSeenTimestamp = firstSeenTimestamp.toLong(),
)

fun RustSessionVerificationRequestDetails.toVerificationRequest(currentUserId: UserId): VerificationRequest.Incoming {
    val details = map()
    return if (currentUserId == details.senderProfile.userId) {
        VerificationRequest.Incoming.OtherSession(details)
    } else {
        VerificationRequest.Incoming.User(details)
    }
}
