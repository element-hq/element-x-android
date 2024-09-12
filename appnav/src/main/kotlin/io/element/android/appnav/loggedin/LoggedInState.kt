/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import io.element.android.libraries.architecture.AsyncData

data class LoggedInState(
    val showSyncSpinner: Boolean,
    val pusherRegistrationState: AsyncData<Unit>,
    val ignoreRegistrationError: Boolean,
    val eventSink: (LoggedInEvents) -> Unit,
)
