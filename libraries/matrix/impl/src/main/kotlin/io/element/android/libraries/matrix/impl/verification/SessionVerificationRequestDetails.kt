/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.verification

import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.FlowId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.verification.SessionVerificationRequestDetails
import org.matrix.rustcomponents.sdk.SessionVerificationRequestDetails as RustSessionVerificationRequestDetails

fun RustSessionVerificationRequestDetails.map() = SessionVerificationRequestDetails(
    senderId = UserId(senderId),
    flowId = FlowId(flowId),
    deviceId = DeviceId(deviceId),
    displayName = displayName,
    firstSeenTimestamp = firstSeenTimestamp.toLong(),
)
