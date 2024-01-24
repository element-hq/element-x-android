/*
 * Copyright (c) 2024 New Vector Ltd
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
import androidx.compose.material.icons.outlined.Description
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
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
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
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackPressed)
                },
                title = {
                    Text(
                        text = state.path,
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                }
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
                            onItemClicked = { onNavigateTo(item) },
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
    onItemClicked: () -> Unit,
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
                onClick = onItemClicked,
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
                onClick = onItemClicked,
            )
        }
        is Item.File -> {
            ListItem(
                leadingContent = ListItemContent.Icon(IconSource.Vector(Icons.Outlined.Description)),
                headlineContent = {
                    Text(
                        text = item.name,
                        modifier = Modifier.padding(16.dp),
                        style = ElementTheme.typography.fontBodyMdMedium,
                    )
                },
                trailingContent = ListItemContent.Text(item.formattedSize),
                onClick = onItemClicked,
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
        onBackPressed = {},
    )
}
