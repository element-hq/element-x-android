/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy.editroomaddress

import androidx.compose.runtime.Composable
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class EditRoomAddressPresenter @Inject constructor() : Presenter<EditRoomAddressState> {

    @Composable
    override fun present(): EditRoomAddressState {

        fun handleEvents(event: EditRoomAddressEvents) {
            when (event) {
                EditRoomAddressEvents.Save -> Unit
            }
        }

        return EditRoomAddressState(
            eventSink = ::handleEvents
        )
    }
}
