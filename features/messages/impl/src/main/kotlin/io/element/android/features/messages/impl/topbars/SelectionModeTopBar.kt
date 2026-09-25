/*
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.topbars

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.SelectionAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionModeTopBar(
    selectedCount: Int,
    action: SelectionAction,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(text = pluralStringResource(R.plurals.screen_room_timeline_selection_title, selectedCount, selectedCount))
        },
        navigationIcon = {
            IconButton(
                onClick = onCancelClick,
            ) {
                Icon(imageVector = CompoundIcons.Close(), contentDescription = stringResource(CommonStrings.action_close))
            }
        },
        actions = {
            IconButton(
                onClick = onConfirmClick,
                enabled = selectedCount > 0,
            ) {
                Icon(
                    resourceId = action.iconRes,
                    contentDescription = stringResource(action.titleRes),
                )
            }
        },
    )
}

@PreviewsDayNight
@Composable
internal fun SelectionModeTopBarPreview(
    @PreviewParameter(SelectionCountProvider::class) count: Int,
) = ElementPreview {
    SelectionModeTopBar(
        selectedCount = count,
        action = SelectionAction.Forward,
        onCancelClick = {},
        onConfirmClick = {},
    )
}

internal class SelectionCountProvider : PreviewParameterProvider<Int> {
    override val values = sequenceOf(0, 1, 5)
}
