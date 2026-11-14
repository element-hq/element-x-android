/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

class ResetIdentityRootStatePreviewParam : PreviewParameterProvider<ResetIdentityRootState> {
    override val values: Sequence<ResetIdentityRootState>
        get() = sequenceOf(
            aResetIdentityRootState(),
            aResetIdentityRootState(displayConfirmationDialog = true),
            aResetIdentityRootState(isResetTheOnlyOption = true, hasEncryptedRooms = AsyncData.Success(true)),
            aResetIdentityRootState(isResetTheOnlyOption = true, hasEncryptedRooms = AsyncData.Success(false)),
            aResetIdentityRootState(isResetTheOnlyOption = true, hasEncryptedRooms = AsyncData.Loading()),
        )
}

fun aResetIdentityRootState(
    isResetTheOnlyOption: Boolean = false,
    hasEncryptedRooms: AsyncData<Boolean> = AsyncData.Uninitialized,
    displayConfirmationDialog: Boolean = false,
    eventSink: (ResetIdentityRootEvent) -> Unit = {},
) = ResetIdentityRootState(
    isResetTheOnlyOption = isResetTheOnlyOption,
    hasEncryptedRooms = hasEncryptedRooms,
    displayConfirmationDialog = displayConfirmationDialog,
    eventSink = eventSink,
)
