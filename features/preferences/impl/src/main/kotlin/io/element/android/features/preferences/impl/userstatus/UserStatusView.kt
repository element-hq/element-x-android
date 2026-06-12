/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
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
                initialEmoji = pickerState.initialEmoji,
                initialText = pickerState.initialText,
                onConfirm = { emoji, text ->
                    state.eventSink(UserStatusEvent.Set(UserStatus(emoji, text)))
                },
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
            Text(
                text = stringResource(R.string.screen_preferences_user_status_placeholder),
                color = ElementTheme.colors.textSecondary,
            )
        },
        leadingContent = {
            Text(text = "🙂", style = ElementTheme.typography.fontBodyLgRegular)
        },
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
        leadingContent = {
            Text(text = emoji, style = ElementTheme.typography.fontBodyLgRegular)
        },
        trailingContent = {
            TextButton(onClick = onClear) {
                Text(text = stringResource(CommonStrings.action_clear))
            }
        },
        modifier = modifier.clickable(onClick = onClick),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserStatusPickerBottomSheet(
    onDismiss: () -> Unit,
    onSelectPredefined: (UserStatus) -> Unit,
    onSelectCustom: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        PredefinedUserStatus.entries.forEach { predefined ->
            val label = stringResource(predefined.labelRes)
            ListItem(
                headlineContent = { Text(text = label) },
                leadingContent = {
                    Text(text = predefined.emoji, style = ElementTheme.typography.fontBodyLgRegular)
                },
                modifier = Modifier.clickable {
                    onSelectPredefined(UserStatus(emoji = predefined.emoji, text = label))
                },
            )
        }
        ListItem(
            headlineContent = {
                Text(text = stringResource(R.string.common_user_status_custom))
            },
            leadingContent = {
                Text(text = "✏️", style = ElementTheme.typography.fontBodyLgRegular)
            },
            modifier = Modifier.clickable(onClick = onSelectCustom),
        )
    }
}

@Composable
private fun CustomStatusInputRow(
    initialEmoji: String,
    initialText: String,
    onConfirm: (emoji: String, text: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var emoji by remember { mutableStateOf(initialEmoji) }
    var text by remember { mutableStateOf(initialText) }

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
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= 30) text = it },
            placeholder = {
                Text(text = stringResource(R.string.screen_preferences_user_status_custom_hint))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (text.isNotBlank()) onConfirm(emoji, text) }
            ),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        TextButton(onClick = onCancel) {
            Text(text = stringResource(CommonStrings.action_cancel))
        }
    }
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
            pickerState = UserStatusPickerState.CustomInput(initialEmoji = "😀", initialText = ""),
            eventSink = {},
        )
    )
}
