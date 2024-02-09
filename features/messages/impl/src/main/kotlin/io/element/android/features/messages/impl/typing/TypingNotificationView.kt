/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.typing

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.room.RoomMember

@Composable
fun TypingNotificationView(
    state: TypingNotificationState,
    modifier: Modifier = Modifier,
) {
    if (state.typingMembers.isEmpty() || !state.renderTypingNotifications) return
    val typingNotificationText = computeTypingNotificationText(state.typingMembers)
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 2.dp),
        text = typingNotificationText,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = ElementTheme.typography.fontBodySmRegular,
        color = ElementTheme.colors.textSecondary,
    )
}

@Composable
private fun computeTypingNotificationText(typingMembers: List<RoomMember>): AnnotatedString {
    val names = when (typingMembers.size) {
        0 -> "" // Cannot happen
        1 -> typingMembers[0].disambiguatedDisplayName
        2 -> stringResource(
            id = R.string.screen_room_typing_two_members,
            typingMembers[0].disambiguatedDisplayName,
            typingMembers[1].disambiguatedDisplayName,
        )
        else -> pluralStringResource(
            id = R.plurals.screen_room_typing_many_members,
            count = typingMembers.size - 2,
            typingMembers[0].disambiguatedDisplayName,
            typingMembers[1].disambiguatedDisplayName,
            typingMembers.size - 2,
        )
    }
    // Get the translated string with a fake pattern
    val tmpString = pluralStringResource(
        id = R.plurals.screen_room_typing_notification,
        count = typingMembers.size,
        "<>",
    )
    // Split the string in 3 parts
    val parts = tmpString.split("<>")
    // And rebuild the string with the names
    return buildAnnotatedString {
        append(parts[0])
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(names)
        }
        append(parts[1])
    }
}

@PreviewsDayNight
@Composable
internal fun TypingNotificationViewPreview(
    @PreviewParameter(TypingNotificationStateProvider::class) state: TypingNotificationState,
) = ElementPreview {
    TypingNotificationView(
        state = state,
    )
}
