/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimelineItemDebugInfo(
    val model: String,
    val originalJson: String?,
    val latestEditedJson: String?,
) : Parcelable
