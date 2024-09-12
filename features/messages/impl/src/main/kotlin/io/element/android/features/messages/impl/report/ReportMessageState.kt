/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.report

import io.element.android.libraries.architecture.AsyncAction

data class ReportMessageState(
    val reason: String,
    val blockUser: Boolean,
    val result: AsyncAction<Unit>,
    val eventSink: (ReportMessageEvents) -> Unit
)
