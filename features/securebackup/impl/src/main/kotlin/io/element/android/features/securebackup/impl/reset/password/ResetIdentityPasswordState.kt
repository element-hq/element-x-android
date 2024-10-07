/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.password

import io.element.android.libraries.architecture.AsyncAction

data class ResetIdentityPasswordState(
    val resetAction: AsyncAction<Unit>,
    val eventSink: (ResetIdentityPasswordEvent) -> Unit,
)
