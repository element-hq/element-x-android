/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import io.element.android.libraries.architecture.AsyncData

data class ResetIdentityRootState(
    /** Whether the user has no other device or recovery key to confirm their identity with. */
    val isResetTheOnlyOption: Boolean,
    /** Whether the user has encrypted chats, only checked and relevant when resetting is the only option. */
    val hasEncryptedRooms: AsyncData<Boolean>,
    val displayConfirmationDialog: Boolean,
    val eventSink: (ResetIdentityRootEvent) -> Unit,
)
