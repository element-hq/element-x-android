/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MessagesSelectionTopBar(
    state: TimelineSelectionState,
    onCancelClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onForwardClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onCancelClick) {
                Icon(
                    imageVector = CompoundIcons.Close(),
                    contentDescription = stringResource(CommonStrings.action_cancel),
                    tint = ElementTheme.colors.iconPrimary,
                )
            }
        },
        title = {
            Text(
                text = if (state.isAtCap) {
                    stringResource(R.string.screen_room_selection_cap_reached)
                } else {
                    pluralStringResource(R.plurals.screen_room_selection_count, state.count, state.count)
                },
                style = ElementTheme.typography.fontHeadingMdRegular,
                color = if (state.isAtCap) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textPrimary,
            )
        },
        actions = {
            IconButton(onClick = onCopyClick) {
                Icon(
                    imageVector = CompoundIcons.Copy(),
                    contentDescription = stringResource(CommonStrings.action_copy),
                    tint = ElementTheme.colors.iconPrimary,
                )
            }
            if (state.canSave) {
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = CompoundIcons.Download(),
                        contentDescription = stringResource(R.string.screen_room_selection_save),
                        tint = ElementTheme.colors.iconPrimary,
                    )
                }
            }
            IconButton(onClick = onForwardClick) {
                Icon(
                    imageVector = CompoundIcons.Forward(),
                    contentDescription = stringResource(CommonStrings.action_forward),
                    tint = ElementTheme.colors.iconPrimary,
                )
            }
            IconButton(
                onClick = onDeleteClick,
                enabled = state.canDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = ElementTheme.colors.iconCriticalPrimary,
                    disabledContentColor = ElementTheme.colors.iconDisabled,
                ),
            ) {
                Icon(
                    imageVector = CompoundIcons.Delete(),
                    contentDescription = stringResource(CommonStrings.action_remove),
                )
            }
        },
    )
}

@PreviewsDayNight
@Composable
internal fun MessagesSelectionTopBarPreview(
    @PreviewParameter(TimelineSelectionStateProvider::class) state: TimelineSelectionState,
) = ElementPreview {
    MessagesSelectionTopBar(
        state = state,
        onCancelClick = {},
        onCopyClick = {},
        onDeleteClick = {},
        onForwardClick = {},
        onSaveClick = {},
    )
}
