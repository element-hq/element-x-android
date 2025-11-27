/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages.sender

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails

data class SenderNameData(
    val userId: UserId,
    val profileDetails: ProfileDetails,
    val senderNameMode: SenderNameMode,
)

open class SenderNameDataProvider : PreviewParameterProvider<SenderNameData> {
    override val values: Sequence<SenderNameData>
        get() = sequenceOf(
            SenderNameMode.Timeline(mainColor = Color.Red),
            SenderNameMode.Reply,
            SenderNameMode.ActionList,
        )
            .flatMap { senderNameMode ->
                sequenceOf(
                    aSenderNameData(
                        senderNameMode = senderNameMode,
                    ),
                    aSenderNameData(
                        senderNameMode = senderNameMode,
                        displayNameAmbiguous = true,
                    ),
                    SenderNameData(
                        senderNameMode = senderNameMode,
                        userId = UserId("@alice:${senderNameMode.javaClass.simpleName.lowercase()}"),
                        profileDetails = ProfileDetails.Unavailable,
                    ),
                )
            }
}

private fun aSenderNameData(
    senderNameMode: SenderNameMode,
    displayNameAmbiguous: Boolean = false,
) = SenderNameData(
    userId = UserId("@alice:${senderNameMode.javaClass.simpleName.lowercase()}"),
    profileDetails = ProfileDetails.Ready(
        displayName = "Alice ${senderNameMode.javaClass.simpleName}",
        displayNameAmbiguous = displayNameAmbiguous,
        avatarUrl = null
    ),
    senderNameMode = senderNameMode,
)
