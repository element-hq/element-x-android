/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.libraries.matrix.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.matrix.ui.media.AvatarAction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarActionBottomSheet(
    actions: ImmutableList<AvatarAction>,
    isVisible: Boolean,
    onSelectAction: (action: AvatarAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    BackHandler(enabled = isVisible) {
        sheetState.hide(coroutineScope, then = { onDismiss() })
    }

    fun onItemActionClick(itemAction: AvatarAction) {
        onSelectAction(itemAction)
        sheetState.hide(coroutineScope, then = { onDismiss() })
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                sheetState.hide(coroutineScope, then = { onDismiss() })
            },
            modifier = modifier,
            sheetState = sheetState,
        ) {
            AvatarActionBottomSheetContent(
                actions = actions,
                onActionClick = ::onItemActionClick,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    }
}

@Composable
private fun AvatarActionBottomSheetContent(
    actions: ImmutableList<AvatarAction>,
    modifier: Modifier = Modifier,
    onActionClick: (AvatarAction) -> Unit = { },
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            items = actions,
        ) { action ->
            ListItem(
                modifier = Modifier.clickable { onActionClick(action) },
                headlineContent = {
                    Text(
                        text = stringResource(action.titleResId),
                        style = ElementTheme.typography.fontBodyLgRegular,
                        color = if (action.destructive) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textPrimary,
                    )
                },
                leadingContent = ListItemContent.Icon(IconSource.Resource(action.iconResourceId)),
                style = when {
                    action.destructive -> ListItemStyle.Destructive
                    else -> ListItemStyle.Primary
                }
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AvatarActionBottomSheetPreview() = ElementPreview {
    AvatarActionBottomSheet(
        actions = persistentListOf(AvatarAction.TakePhoto, AvatarAction.ChoosePhoto, AvatarAction.Remove),
        isVisible = true,
        onSelectAction = { },
        onDismiss = { },
    )
}
