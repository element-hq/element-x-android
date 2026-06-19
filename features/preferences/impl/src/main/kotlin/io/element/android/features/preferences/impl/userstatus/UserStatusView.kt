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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun UserStatusView(
    state: UserStatusState,
    modifier: Modifier = Modifier,
) {
    when (val pickerState = state.pickerState) {
        UserStatusPickerState.Hidden, UserStatusPickerState.ShowingPicker -> {
            val displayedStatus = state.displayedStatus
            if (displayedStatus != null) {
                CurrentStatusRow(
                    displayedStatus = displayedStatus,
                    onClick = { state.eventSink(UserStatusEvent.OpenPicker) },
                    onClear = { state.eventSink(UserStatusEvent.ClearStatus) },
                    modifier = modifier,
                )
            } else {
                EmptyStatusRow(
                    onClick = { state.eventSink(UserStatusEvent.OpenPicker) },
                    modifier = modifier,
                )
            }
            if (pickerState == UserStatusPickerState.ShowingPicker) {
                UserStatusPickerBottomSheet(
                    currentRawStatus = state.rawStatus,
                    onDismiss = { state.eventSink(UserStatusEvent.DismissPicker) },
                    onSelectPredefinedStatus = { status -> state.eventSink(UserStatusEvent.SetStatus(status)) },
                    onSelectCustomStatus = { state.eventSink(UserStatusEvent.OpenCustomInput) },
                )
            }
        }
        is UserStatusPickerState.CustomInput -> {
            CustomStatusInputRow(
                emoji = pickerState.emoji,
                textFieldState = pickerState.textFieldState,
                rawStatus = state.rawStatus,
                onConfirm = {
                    state.eventSink(
                        UserStatusEvent.SetStatus(UserStatus(pickerState.emoji, pickerState.textFieldState.text.toString()))
                    )
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
                modifier = Modifier.padding(vertical = 16.dp),
            )
        },
        trailingContent = ListItemContent.Custom({
            Box(modifier = Modifier.minimumInteractiveComponentSize())
        }),
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
        headlineContent = { Text(text = text, modifier = Modifier.padding(vertical = 16.dp)) },
        leadingContent = ListItemContent.Custom({
            Text(text = emoji, modifier = Modifier.size(24.dp), textAlign = TextAlign.Center)
        }),
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
    currentRawStatus: UserStatus?,
    onDismiss: () -> Unit,
    onSelectPredefinedStatus: (UserStatus) -> Unit,
    onSelectCustomStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        scrollable = true,
        modifier = modifier,
    ) {
        PredefinedUserStatus.entries.forEach { predefined ->
            val label = stringResource(predefined.labelRes)
            val predefinedUserStatus = UserStatus(emoji = predefined.emoji, text = label)
            val isSelected = currentRawStatus == predefinedUserStatus
            ListItem(
                headlineContent = { Text(text = label) },
                leadingContent = ListItemContent.Text(text = predefined.emoji),
                trailingContent = if (isSelected) {
                    ListItemContent.Custom({
                    Icon(
                        imageVector = CompoundIcons.Check(),
                        contentDescription = null,
                        tint = ElementTheme.colors.iconAccentPrimary,
                    )
                })
                } else {
                    null
                },
                onClick = {
                    sheetState.hide(coroutineScope) {
                        onSelectPredefinedStatus(predefinedUserStatus)
                    }
                },
            )
        }
        ListItem(
            headlineContent = {
                Text(text = stringResource(R.string.common_user_status_custom))
            },
            leadingContent = ListItemContent.Text(text = "✏️"),
            onClick = {
                sheetState.hide(coroutineScope) {
                    onSelectCustomStatus()
                }
            },
        )
    }
}

private const val CUSTOM_STATUS_TEXT_MAX_LENGTH = 30

@Composable
private fun CustomStatusInputRow(
    emoji: String,
    textFieldState: TextFieldState,
    rawStatus: UserStatus?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasText by remember { derivedStateOf { textFieldState.text.isNotEmpty() } }
    val hasChanges by remember(emoji, rawStatus) {
        derivedStateOf {
            val text = textFieldState.text.toString()
            text.isNotBlank() && UserStatus(emoji, text) != rawStatus
        }
    }
    val saveLabel = stringResource(CommonStrings.action_save)
    val cancelLabel = stringResource(CommonStrings.action_cancel)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    ListItem(
        headlineContent = {
            TextField(
                modifier = Modifier.focusRequester(focusRequester),
                state = textFieldState,
                placeholder = stringResource(R.string.screen_preferences_user_status_custom_hint),
                inputTransformation = InputTransformation.maxLength(CUSTOM_STATUS_TEXT_MAX_LENGTH),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                onKeyboardAction = { if (hasChanges) onConfirm() },
                lineLimits = TextFieldLineLimits.SingleLine,
                trailingIcon = if (hasText) {
                    {
                        Box(modifier = Modifier.clickable { textFieldState.clearText() }) {
                            Icon(
                                imageVector = CompoundIcons.Close(),
                                contentDescription = stringResource(CommonStrings.action_cancel),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                } else {
                    null
                }
            )
        },
        trailingContent = ListItemContent.Custom({
            // Layout measures both buttons to determine the max natural width,
            // then places only the active one — no hardcoded padding values needed.
            Layout(
                content = {
                    TextButton(onClick = onConfirm, text = saveLabel)
                    TextButton(onClick = onCancel, text = cancelLabel)
                }
            ) { measurables, constraints ->
                val placeables = measurables.map { it.measure(Constraints()) }
                val maxWidth = placeables.maxOf { it.width }
                val maxHeight = placeables.maxOf { it.height }
                layout(maxWidth, maxHeight) {
                    if (hasChanges) {
                        placeables[0].placeRelative(0, 0)
                    } else {
                        placeables[1].placeRelative(0, 0)
                    }
                }
            }
        }),
        leadingContent = ListItemContent.Custom({
            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, ElementTheme.colors.bgSubtleSecondary),
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable { },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, style = ElementTheme.typography.fontBodyLgRegular)
                }
            }
        }),
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun UserStatusViewPreview(@PreviewParameter(UserStatusStateProvider::class) state: UserStatusState) = ElementPreview {
    UserStatusView(state = state)
}
