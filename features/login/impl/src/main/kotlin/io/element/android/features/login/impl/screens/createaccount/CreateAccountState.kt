/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.SessionId

data class CreateAccountState(
    val url: String,
    val pageProgress: Int,
    val createAction: AsyncAction<SessionId>,
    val isDebugBuild: Boolean,
    val eventSink: (CreateAccountEvents) -> Unit
)
