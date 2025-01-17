/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy.editroomaddress

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class EditRoomAddressStateProvider : PreviewParameterProvider<EditRoomAddressState> {
    override val values: Sequence<EditRoomAddressState>
        get() = sequenceOf(
            aEditRoomAddressState(),
            // Add other states here
        )
}

fun aEditRoomAddressState() = EditRoomAddressState(
    eventSink = {}
)
