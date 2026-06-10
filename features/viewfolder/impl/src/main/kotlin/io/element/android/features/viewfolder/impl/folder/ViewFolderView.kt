/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.folder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.SubdirectoryArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFolderView(
    state: ViewFolderState,
    onNavigateTo: (Item) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClick)
                },
                titleStr = state.title,
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = state.content,
                    ) { item ->
                        ItemRow(
                            item = item,
                            onItemClick = { onNavigateTo(item) },
                        )
                    }
                    if (state.content.none { it !is Item.Parent }) {
                        item {
                            Spacer(Modifier.size(80.dp))
                            Text(
                                text = "Empty folder",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ItemRow(
    item: Item,
    onItemClick: () -> Unit,
) {
    when (item) {
        Item.Parent -> {
            ListItem(
                leadingContent = ListItemContent.Icon(IconSource.Vector(Icons.Outlined.SubdirectoryArrowLeft)),
                headlineContent = {
                    Text(
                        text = "..",
                        modifier = Modifier.padding(16.dp),
                        style = ElementTheme.typography.fontBodyMdMedium,
                    )
                },
                onClick = onItemClick,
            )
        }
        is Item.Folder -> {
            ListItem(
                leadingContent = ListItemContent.Icon(IconSource.Vector(Icons.Outlined.Folder)),
                headlineContent = {
                    Text(
                        text = item.name,
                        modifier = Modifier.padding(16.dp),
                        style = ElementTheme.typography.fontBodyMdMedium,
                    )
                },
                onClick = onItemClick,
            )
        }
        is Item.File -> {
            ListItem(
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Document())),
                headlineContent = {
                    Text(
                        text = item.name,
                        modifier = Modifier.padding(16.dp),
                        style = ElementTheme.typography.fontBodyMdMedium,
                    )
                },
                trailingContent = ListItemContent.Text(item.formattedSize),
                onClick = onItemClick,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ViewFolderViewPreview(@PreviewParameter(ViewFolderStateProvider::class) state: ViewFolderState) = ElementPreview {
    ViewFolderView(
        state = state,
        onNavigateTo = {},
        onBackClick = {},
    )
}
