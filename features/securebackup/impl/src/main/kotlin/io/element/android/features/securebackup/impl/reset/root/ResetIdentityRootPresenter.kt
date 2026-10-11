/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.logout.api.direct.DirectLogoutEvent
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient

@AssistedInject
class ResetIdentityRootPresenter(
    @Assisted private val hasConfirmationOptions: Boolean,
    private val matrixClient: MatrixClient,
    private val directLogoutPresenter: Presenter<DirectLogoutState>,
) : Presenter<ResetIdentityRootState> {
    @AssistedFactory
    interface Factory {
        fun create(hasConfirmationOptions: Boolean): ResetIdentityRootPresenter
    }

    @Composable
    override fun present(): ResetIdentityRootState {
        var displayConfirmDialog by remember { mutableStateOf(false) }
        val directLogoutState = directLogoutPresenter.present()

        val variant by produceState<ResetIdentityRootVariant?>(
            initialValue = ResetIdentityRootVariant.HasConfirmationOptions.takeIf { hasConfirmationOptions },
        ) {
            if (!hasConfirmationOptions) {
                value = if (matrixClient.hasEncryptedRooms().getOrDefault(true)) {
                    ResetIdentityRootVariant.NoOptionsWithEncryptedChats
                } else {
                    ResetIdentityRootVariant.NoOptionsWithoutEncryptedChats
                }
            }
        }

        fun handleEvent(event: ResetIdentityRootEvent) {
            when (event) {
                ResetIdentityRootEvent.Continue -> displayConfirmDialog = true
                ResetIdentityRootEvent.DismissDialog -> displayConfirmDialog = false
                ResetIdentityRootEvent.SignOut -> directLogoutState.eventSink(DirectLogoutEvent.Logout(ignoreSdkError = false))
            }
        }

        return ResetIdentityRootState(
            variant = variant,
            displayConfirmationDialog = displayConfirmDialog,
            directLogoutState = directLogoutState,
            eventSink = ::handleEvent,
        )
    }
}
