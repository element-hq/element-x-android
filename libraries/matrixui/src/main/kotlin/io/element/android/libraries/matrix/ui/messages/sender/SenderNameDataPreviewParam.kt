/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages.sender

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.matrix.ui.messages.reply.aProfileDetailsReady

data class SenderNameData(
    val userId: UserId,
    val profileDetails: ProfileDetails,
)

open class SenderNameDataPreviewParam : PreviewParameterProvider<SenderNameData> {
    override val values: Sequence<SenderNameData>
        get() = sequenceOf(
            aSenderNameData(),
            aSenderNameData(
                displayNameAmbiguous = true,
            ),
            aSenderNameData(
                profileDetails = ProfileDetails.Unavailable,
            ),
            aSenderNameData(
                displayName = null,
            ),
            aSenderNameData(
                displayedStatus = DisplayedStatus.UserSet(
                    status = UserStatus(
                        emoji = "😀",
                        text = "Should not be rendered",
                    ),
                ),
            ),
            aSenderNameData(
                displayedStatus = DisplayedStatus.InCall(0L),
            ),
            aSenderNameData(
                displayNameAmbiguous = true,
                displayedStatus = DisplayedStatus.UserSet(
                    status = UserStatus(
                        emoji = "😀",
                        text = "Should not be rendered",
                    ),
                ),
            ),
        )
}

private fun aSenderNameData(
    userId: UserId = UserId("@alice:example.com"),
    displayName: String? = "Alice",
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
)
