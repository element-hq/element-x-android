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

package io.element.android.features.poll.impl.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.VectorIcons
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.theme.ElementTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollView(
    state: CreatePollState,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Poll",
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
                navigationIcon = {
                    BackButton(onClick = navigateUp)
                },
                actions = {
                    TextButton(
                        text = "Create",
                        onClick = {
                            state.eventSink(CreatePollEvents.Create)
                            navigateUp()
                        },
                        enabled = state.canCreate,
                    )
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize(),
        ) {
            LazyColumn {
                item {
                    Text(
                        text = "Question or topic",
                        modifier = Modifier.padding(start = 32.dp),
                        style = ElementTheme.typography.fontBodyMdRegular,
                    )
                }
                item {
                    ListItem(
                        headlineContent = {
                            OutlinedTextField(
                                value = state.question,
                                onValueChange = {
                                    state.eventSink(CreatePollEvents.SetQuestion(it))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(text = "What is the poll about?")
                                },
                            )
                        }
                    )
                }
                itemsIndexed(state.answers) { index, answer ->
                    ListItem(
                        headlineContent = {
                            OutlinedTextField(
                                value = answer.text,
                                onValueChange = {
                                    state.eventSink(CreatePollEvents.SetAnswer(index, it))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(text = "Option ${index + 1}")
                                },
                            )
                        },
                        // TODO animate this when https://issuetracker.google.com/issues/150812265 is done.
                        // TODO modifier = Modifier.animateItemPlacement(),
                        trailingContent = ListItemContent.Custom {
                            Icon(
                                resourceId = VectorIcons.Delete,
                                contentDescription = null,
                                modifier = Modifier.clickable(answer.canDelete) {
                                    state.eventSink(CreatePollEvents.RemoveAnswer(index))
                                },
                            )
                        },
                        style = if (answer.canDelete) ListItemStyle.Destructive else ListItemStyle.Default,
                    )
                }
                if (state.canAddAnswer) {
                    item {
                        ListItem(
                            headlineContent = { Text(text = "Add option") },
                            leadingContent = ListItemContent.Icon(
                                iconSource = IconSource.Vector(Icons.Default.Add),
                            ),
                            style = ListItemStyle.Primary,
                            onClick = { state.eventSink(CreatePollEvents.AddAnswer) },
                        )
                    }
                }
                item {
                    HorizontalDivider()
                }
                item {
                    ListItem(
                        headlineContent = { Text(text = "Anonymous Poll") },
                        supportingContent = { Text(text = "Show results only after poll ends") },
                        trailingContent = ListItemContent.Switch(
                            checked = state.pollKind == PollKind.Undisclosed,
                            onChange = { state.eventSink(CreatePollEvents.SetPollKind(if (it) PollKind.Undisclosed else PollKind.Disclosed)) },
                        ),
                    )
                }
            }
        }
    }
}

@DayNightPreviews
@Composable
internal fun CreatePollViewPreview(
    @PreviewParameter(CreatePollStateProvider::class) state: CreatePollState
) = ElementPreview {
    CreatePollView(
        state = state,
        navigateUp = {},
    )
}
