/*
 * Copyright (c) 2023 New Vector Ltd
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

@file:OptIn(ExperimentalMaterialApi::class)
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheetLayout
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.compound.theme.ElementTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun AvatarActionBottomSheet(
    actions: ImmutableList<AvatarAction>,
    modalBottomSheetState: ModalBottomSheetState,
    onActionSelected: (action: AvatarAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    fun onItemActionClicked(itemAction: AvatarAction) {
        onActionSelected(itemAction)
        coroutineScope.launch {
            modalBottomSheetState.hide()
        }
    }

    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = modalBottomSheetState,
        displayHandle = true,
        sheetContent = {
            AvatarActionBottomSheetContent(
                actions = actions,
                onActionClicked = ::onItemActionClicked,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    )
}

@Composable
private fun AvatarActionBottomSheetContent(
    actions: ImmutableList<AvatarAction>,
    modifier: Modifier = Modifier,
    onActionClicked: (AvatarAction) -> Unit = { },
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            items = actions,
        ) { action ->
            ListItem(
                modifier = Modifier.clickable { onActionClicked(action) },
                headlineContent = {
                    Text(
                        text = stringResource(action.titleResId),
                        style = ElementTheme.typography.fontBodyLgRegular,
                        color = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
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
        modalBottomSheetState = ModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Expanded,
            density = LocalDensity.current,
        ),
        onActionSelected = { },
    )
}
