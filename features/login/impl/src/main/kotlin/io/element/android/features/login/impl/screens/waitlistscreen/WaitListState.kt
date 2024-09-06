/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.waitlistscreen

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.SessionId

// Do not use default value, so no member get forgotten in the presenters.
data class WaitListState(
    val appName: String,
    val serverName: String,
    val loginAction: AsyncData<SessionId>,
    val eventSink: (WaitListEvents) -> Unit
)
