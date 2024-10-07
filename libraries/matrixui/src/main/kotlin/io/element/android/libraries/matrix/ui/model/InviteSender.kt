/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.ui.R

@Immutable
data class InviteSender(
    val userId: UserId,
    val displayName: String,
    val avatarData: AvatarData,
) {
    @Composable
    fun annotatedString(): AnnotatedString {
        return stringResource(R.string.screen_invites_invited_you, displayName, userId.value).let { text -> // TCHAP TODO should be changed to hide the user id
            val senderNameStart = LocalContext.current.getString(R.string.screen_invites_invited_you).indexOf("%1\$s")
            AnnotatedString(
                text = text,
                spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        start = senderNameStart,
                        end = senderNameStart + displayName.length
                    )
                )
            )
        }
    }
}

fun RoomMember.toInviteSender() = InviteSender(
    userId = userId,
    displayName = displayName ?: "",
    avatarData = getAvatarData(size = AvatarSize.InviteSender),
)
