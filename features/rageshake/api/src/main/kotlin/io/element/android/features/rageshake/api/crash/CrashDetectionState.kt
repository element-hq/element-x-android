/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.crash

import androidx.compose.runtime.Immutable

@Immutable
data class CrashDetectionState(
    val appName: String,
    val crashDetected: Boolean,
    val eventSink: (CrashDetectionEvents) -> Unit
)
