/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.api.direct

import io.element.android.libraries.architecture.AsyncAction

data class DirectLogoutState(
    val canDoDirectSignOut: Boolean,
    val logoutAction: AsyncAction<String?>,
    val eventSink: (DirectLogoutEvents) -> Unit,
)
