/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import io.element.android.libraries.architecture.AsyncData

data class CallScreenState(
    val urlState: AsyncData<String>,
    val webViewError: String?,
    val userAgent: String,
    val isCallActive: Boolean,
    val isInWidgetMode: Boolean,
    val eventSink: (CallScreenEvents) -> Unit,
)
