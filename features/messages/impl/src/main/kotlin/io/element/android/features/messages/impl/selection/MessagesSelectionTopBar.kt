/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentSetOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MessagesSelectionTopBar(
    state: TimelineSelectionState,
    canDeleteSelection: Boolean,
    onCancelClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onForwardClick: () -> Unit,
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
                // At the cap the title itself states the limit instead of firing a snackbar -
                // cheaper, never floods, and stays on this screen.
                text = if (state.isAtCap) {
                    stringResource(R.string.screen_messages_selection_cap_reached)
                } else {
                    pluralStringResource(R.plurals.screen_messages_selection_count_short, state.count, state.count)
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
            IconButton(onClick = onForwardClick) {
                Icon(
                    imageVector = CompoundIcons.Forward(),
                    contentDescription = stringResource(CommonStrings.action_forward),
                    tint = ElementTheme.colors.iconPrimary,
                )
            }
            // Always show Delete, but disable it when the selection contains a message the user
            // is not allowed to redact (or has no redact rights at all). Compound greys out a
            // disabled IconButton automatically.
            IconButton(
                onClick = onDeleteClick,
                enabled = canDeleteSelection,
            ) {
                Icon(
                    imageVector = CompoundIcons.Delete(),
                    contentDescription = stringResource(CommonStrings.action_remove),
                    // Critical red when actionable, muted grey when disabled, so the inactive state reads clearly.
                    tint = if (canDeleteSelection) ElementTheme.colors.iconCriticalPrimary else ElementTheme.colors.iconDisabled,
                )
            }
        },
    )
}

@PreviewsDayNight
@Composable
internal fun MessagesSelectionTopBarPreview() = ElementPreview {
    MessagesSelectionTopBar(
        state = TimelineSelectionState(
            isActive = true,
            selectedIds = persistentSetOf(EventId("\$1"), EventId("\$2"), EventId("\$3")),
            maxSelection = TimelineSelectionState.MAX_SELECTION,
        ),
        canDeleteSelection = true,
        onCancelClick = {},
        onCopyClick = {},
        onDeleteClick = {},
        onForwardClick = {},
    )
}

@PreviewsDayNight
@Composable
internal fun MessagesSelectionTopBarAtCapPreview() = ElementPreview {
    MessagesSelectionTopBar(
        state = TimelineSelectionState(
            isActive = true,
            selectedIds = persistentSetOf(EventId("\$1")),
            maxSelection = 1,
        ),
        canDeleteSelection = true,
        onCancelClick = {},
        onCopyClick = {},
        onDeleteClick = {},
        onForwardClick = {},
    )
}
