/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import kotlinx.collections.immutable.ImmutableList

data class TimelineItemReadReceipts(
    val receipts: ImmutableList<ReadReceiptData>,
)

data class ReadReceiptData(
    val avatarData: AvatarData,
    val formattedDate: String,
)
