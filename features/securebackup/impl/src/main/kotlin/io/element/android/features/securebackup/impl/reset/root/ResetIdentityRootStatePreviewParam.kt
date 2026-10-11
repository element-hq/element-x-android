/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.logout.api.direct.aDirectLogoutState

class ResetIdentityRootStatePreviewParam : PreviewParameterProvider<ResetIdentityRootState> {
    override val values: Sequence<ResetIdentityRootState>
        get() = sequenceOf(
            aResetIdentityRootState(),
            aResetIdentityRootState(displayConfirmationDialog = true),
            aResetIdentityRootState(variant = ResetIdentityRootVariant.NoOptionsWithEncryptedChats),
            aResetIdentityRootState(variant = ResetIdentityRootVariant.NoOptionsWithoutEncryptedChats),
            aResetIdentityRootState(variant = null),
        )
}

fun aResetIdentityRootState(
    variant: ResetIdentityRootVariant? = ResetIdentityRootVariant.HasConfirmationOptions,
    displayConfirmationDialog: Boolean = false,
    eventSink: (ResetIdentityRootEvent) -> Unit = {},
) = ResetIdentityRootState(
    variant = variant,
    displayConfirmationDialog = displayConfirmationDialog,
    directLogoutState = aDirectLogoutState(),
    eventSink = eventSink,
)
