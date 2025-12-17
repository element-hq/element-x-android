/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.verification

import android.os.Parcelable
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.FlowId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessionVerificationRequestDetails(
    val senderProfile: MatrixUser,
    val flowId: FlowId,
    val deviceId: DeviceId,
    val deviceDisplayName: String?,
    val firstSeenTimestamp: Long,
) : Parcelable
