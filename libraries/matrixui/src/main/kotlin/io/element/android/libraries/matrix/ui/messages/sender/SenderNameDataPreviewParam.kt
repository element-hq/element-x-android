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
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.matrix.ui.messages.reply.aProfileDetailsReady

data class SenderNameData(
    val userId: UserId,
    val profileDetails: ProfileDetails,
    val senderNameMode: SenderNameMode,
)

open class SenderNameDataPreviewParam : PreviewParameterProvider<SenderNameData> {
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
                    aSenderNameData(
                        senderNameMode = senderNameMode,
                        profileDetails = ProfileDetails.Unavailable,
                    ),
                    aSenderNameData(
                        senderNameMode = senderNameMode,
                        displayName = null,
                    ),
                    aSenderNameData(
                        senderNameMode = senderNameMode,
                        displayedStatus = DisplayedStatus.UserSet(
                            status = UserStatus(
                                emoji = "😀",
                                text = "Should not be rendered",
                            ),
                        ),
                    ),
                    aSenderNameData(
                        senderNameMode = senderNameMode,
                        displayedStatus = DisplayedStatus.InCall(0L),
                    )
                )
            }
}

private fun aSenderNameData(
    senderNameMode: SenderNameMode,
    userId: UserId = UserId("@alice:${senderNameMode.javaClass.simpleName.lowercase()}"),
    displayName: String? = "Alice ${senderNameMode.javaClass.simpleName}",
    displayNameAmbiguous: Boolean = false,
    displayedStatus: DisplayedStatus? = null,
    profileDetails: ProfileDetails = aProfileDetailsReady(
        displayName = displayName,
        displayNameAmbiguous = displayNameAmbiguous,
        displayedStatus = displayedStatus,
    )
) = SenderNameData(
    userId = userId,
    profileDetails = profileDetails,
    senderNameMode = senderNameMode,
)
