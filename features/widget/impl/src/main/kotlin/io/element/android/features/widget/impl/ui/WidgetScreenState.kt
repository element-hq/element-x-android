/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.impl.ui

import io.element.android.libraries.architecture.AsyncData

data class WidgetScreenState(
    val urlState: AsyncData<String>,
    val webViewError: String?,
    val userAgent: String,
    val isWidgetLoaded: Boolean,
    val isInWidgetMode: Boolean,
    val eventSink: (WidgetScreenEvents) -> Unit,
)

