/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun UserStatusRow(
    state: UserStatusState,
    modifier: Modifier = Modifier,
) {
    when (val pickerState = state.pickerState) {
        UserStatusPickerState.Hidden, UserStatusPickerState.ShowingPicker -> {
            val displayedStatus = state.displayedStatus
            if (displayedStatus != null) {
                CurrentStatusRow(
                    displayedStatus = displayedStatus,
                    onClick = { state.eventSink(UserStatusEvent.Open) },
                    onClear = { state.eventSink(UserStatusEvent.Clear) },
                    modifier = modifier,
                )
            } else {
                EmptyStatusRow(
                    onClick = { state.eventSink(UserStatusEvent.Open) },
                    modifier = modifier,
                )
            }
            if (pickerState == UserStatusPickerState.ShowingPicker) {
                UserStatusPickerBottomSheet(
                    onDismiss = { state.eventSink(UserStatusEvent.Dismiss) },
                    onSelectPredefined = { status -> state.eventSink(UserStatusEvent.Set(status)) },
                    onSelectCustom = { state.eventSink(UserStatusEvent.OpenCustomInput) },
                )
            }
        }
        is UserStatusPickerState.CustomInput -> {
            CustomStatusInputRow(
                emoji = pickerState.emoji,
                text = pickerState.text,
                onEmojiChange = { state.eventSink(UserStatusEvent.UpdateCustomEmoji(it)) },
                onTextChange = { state.eventSink(UserStatusEvent.UpdateCustomText(it)) },
                onConfirm = { state.eventSink(UserStatusEvent.Set(UserStatus(pickerState.emoji, pickerState.text))) },
                onCancel = { state.eventSink(UserStatusEvent.CancelCustomInput) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun EmptyStatusRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.screen_preferences_user_status_placeholder))
        },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Reaction())),
        modifier = modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun CurrentStatusRow(
    displayedStatus: DisplayedStatus,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (emoji, text) = when (displayedStatus) {
        is DisplayedStatus.UserSet -> displayedStatus.status.emoji to displayedStatus.status.text
        is DisplayedStatus.InCall -> "🎧" to stringResource(R.string.common_user_status_on_a_call)
    }
    ListItem(
        headlineContent = { Text(text = text) },
        leadingContent = ListItemContent.Text(emoji),
        trailingContent = ListItemContent.Custom({
            IconButton(onClick = onClear) {
                Icon(imageVector = CompoundIcons.Close(), contentDescription = null)
            }
        }),
        onClick = onClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserStatusPickerBottomSheet(
    onDismiss: () -> Unit,
    onSelectPredefined: (UserStatus) -> Unit,
    onSelectCustom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, scrollable = true, modifier = modifier) {
        PredefinedUserStatus.entries.forEach { predefined ->
            val label = stringResource(predefined.labelRes)
            ListItem(
                headlineContent = { Text(text = label) },
                leadingContent = ListItemContent.Text(text = predefined.emoji),
                onClick = {
                    onSelectPredefined(UserStatus(emoji = predefined.emoji, text = label))
                },
            )
        }
        ListItem(
            headlineContent = {
                Text(text = stringResource(R.string.common_user_status_custom))
            },
            leadingContent = ListItemContent.Text(text = "✏️"),
            onClick = onSelectCustom,
        )
    }
}

@Composable
private fun CustomStatusInputRow(
    emoji: String,
    text: String,
    onEmojiChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            TextField(
                value = text,
                onValueChange = { if (it.length <= 30) onTextChange(it) },
                placeholder = stringResource(R.string.screen_preferences_user_status_custom_hint),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (text.isNotBlank()) onConfirm() }
                ),
                singleLine = true,
            )
        },
        trailingContent = ListItemContent.Custom({
            TextButton(onClick = onCancel, text = stringResource(CommonStrings.action_cancel))
        }),
        leadingContent = ListItemContent.Custom( {
            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, ElementTheme.colors.bgSubtleSecondary),
                modifier = Modifier
                    .size(50.dp)
                    .clickable { },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, style = ElementTheme.typography.fontBodyLgRegular)
                }
            }
        })
    )

    /*
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = ElementTheme.colors.bgSubtleSecondary,
            modifier = Modifier
                .size(40.dp)
                .clickable { },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, style = ElementTheme.typography.fontBodyLgRegular)
            }
        }
        TextField(
            value = text,
            onValueChange = { if (it.length <= 30) text = it },
            placeholder = stringResource(R.string.screen_preferences_user_status_custom_hint),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (text.isNotBlank()) onConfirm(emoji, text) }
            ),
            singleLine = true,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        TextButton(onClick = onCancel, text = stringResource(CommonStrings.action_cancel))
    }

     */
}

@PreviewsDayNight
@Composable
internal fun UserStatusRowEmptyPreview() = ElementPreview {
    UserStatusRow(
        state = UserStatusState(
            displayedStatus = null,
            pickerState = UserStatusPickerState.Hidden,
            eventSink = {},
        )
    )
}

@PreviewsDayNight
@Composable
internal fun UserStatusRowSetPreview() = ElementPreview {
    UserStatusRow(
        state = UserStatusState(
            displayedStatus = DisplayedStatus.UserSet(UserStatus("🌴", "Away")),
            pickerState = UserStatusPickerState.Hidden,
            eventSink = {},
        )
    )
}

@PreviewsDayNight
@Composable
internal fun UserStatusRowCustomInputPreview() = ElementPreview {
    UserStatusRow(
        state = UserStatusState(
            displayedStatus = null,
            pickerState = UserStatusPickerState.CustomInput(emoji = "😀", text = ""),
            eventSink = {},
        )
    )
}
