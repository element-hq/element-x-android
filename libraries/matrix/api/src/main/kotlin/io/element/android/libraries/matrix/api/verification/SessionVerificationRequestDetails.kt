/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.verification

import android.os.Parcelable
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.FlowId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessionVerificationRequestDetails(
    val senderId: UserId,
    val flowId: FlowId,
    val deviceId: DeviceId,
    val displayName: String?,
    val firstSeenTimestamp: Long,
) : Parcelable
