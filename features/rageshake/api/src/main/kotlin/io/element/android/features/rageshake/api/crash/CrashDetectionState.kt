/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.crash

data class CrashDetectionState(
    val appName: String,
    val crashDetected: Boolean,
    val eventSink: (CrashDetectionEvents) -> Unit
)
