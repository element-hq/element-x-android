/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.typing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import kotlinx.collections.immutable.ImmutableList

@Suppress("MultipleEmitters") // False positive
@Composable
fun TypingNotificationView(
    state: TypingNotificationState,
    modifier: Modifier = Modifier,
) {
    val displayNotifications = state.typingMembers.isNotEmpty() && state.renderTypingNotifications

    @Suppress("ModifierNaming")
    @Composable
    fun TypingText(text: AnnotatedString, textModifier: Modifier = Modifier) {
        Text(
            modifier = textModifier,
            text = text,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary,
        )
    }

    // Display the typing notification space when either a typing notification needs to be displayed or a previous one already was
    AnimatedVisibility(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        visible = displayNotifications || state.reserveSpace,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val typingNotificationText = computeTypingNotificationText(state.typingMembers)
        Box(contentAlignment = Alignment.BottomStart) {
            // Reserve the space for the typing notification by adding an invisible text
            TypingText(
                text = typingNotificationText,
                textModifier = Modifier
                    .alpha(0f)
                    // Remove the semantics of the text to avoid screen readers to read it
                    .clearAndSetSemantics { }
            )

            // Display the actual notification
            AnimatedVisibility(
                visible = displayNotifications,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                TypingText(text = typingNotificationText, textModifier = Modifier.padding(horizontal = 24.dp))
            }
        }
    }
}

@Composable
private fun computeTypingNotificationText(typingMembers: ImmutableList<TypingRoomMember>): AnnotatedString {
    // Remember the last value to avoid empty typing messages while animating
    var result by remember { mutableStateOf(AnnotatedString("")) }
    if (typingMembers.isNotEmpty()) {
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
        result = buildAnnotatedString {
            append(parts[0])
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(names)
            }
            append(parts[1])
        }
    }
    return result
}

@PreviewsDayNight
@Composable
internal fun TypingNotificationViewPreview(
    @PreviewParameter(TypingNotificationStateProvider::class) state: TypingNotificationState,
) = ElementPreview {
    TypingNotificationView(
        modifier = if (state.reserveSpace) Modifier.border(1.dp, Color.Blue) else Modifier,
        state = state,
    )
}
