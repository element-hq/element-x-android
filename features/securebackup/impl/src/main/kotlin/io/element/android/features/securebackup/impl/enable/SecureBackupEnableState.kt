/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enable

import io.element.android.libraries.architecture.AsyncAction

data class SecureBackupEnableState(
    val enableAction: AsyncAction<Unit>,
    val eventSink: (SecureBackupEnableEvents) -> Unit
)
