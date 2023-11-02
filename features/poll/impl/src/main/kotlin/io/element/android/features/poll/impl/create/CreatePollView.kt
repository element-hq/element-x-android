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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.poll.impl.R
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
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
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollView(
    state: CreatePollState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    val navBack = { state.eventSink(CreatePollEvents.ConfirmNavBack) }
    BackHandler(onBack = navBack)
    if (state.showConfirmation) ConfirmationDialog(
        content = stringResource(id = R.string.screen_create_poll_discard_confirmation),
        onSubmitClicked = { state.eventSink(CreatePollEvents.NavBack) },
        onDismiss = { state.eventSink(CreatePollEvents.HideConfirmation) }
    )
    val questionFocusRequester = remember { FocusRequester() }
    val answerFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        questionFocusRequester.requestFocus()
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.screen_create_poll_title),
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
                navigationIcon = {
                    BackButton(onClick = navBack)
                },
                actions = {
                    TextButton(
                        text = stringResource(id = CommonStrings.action_create),
                        onClick = { state.eventSink(CreatePollEvents.Create) },
                        enabled = state.canCreate,
                    )
                }
            )
        },
    ) { paddingValues ->
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .fillMaxSize(),
            state = lazyListState,
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(id = R.string.screen_create_poll_question_desc),
                        modifier = Modifier.padding(start = 32.dp),
                        style = ElementTheme.typography.fontBodyMdRegular,
                    )
                    ListItem(
                        headlineContent = {
                            OutlinedTextField(
                                value = state.question,
                                onValueChange = {
                                    state.eventSink(CreatePollEvents.SetQuestion(it))
                                },
                                modifier = Modifier
                                    .focusRequester(questionFocusRequester)
                                    .fillMaxWidth(),
                                placeholder = {
                                    Text(text = stringResource(id = R.string.screen_create_poll_question_hint))
                                },
                                keyboardOptions = keyboardOptions,
                            )
                        }
                    )
                }
            }
            itemsIndexed(state.answers) { index, answer ->
                val isLastItem = index == state.answers.size - 1
                ListItem(
                    headlineContent = {
                        OutlinedTextField(
                            value = answer.text,
                            onValueChange = {
                                state.eventSink(CreatePollEvents.SetAnswer(index, it))
                            },
                            modifier = Modifier
                                .then(if (isLastItem) Modifier.focusRequester(answerFocusRequester) else Modifier)
                                .fillMaxWidth(),
                            placeholder = {
                                Text(text = stringResource(id = R.string.screen_create_poll_answer_hint, index + 1))
                            },
                            keyboardOptions = keyboardOptions,
                        )
                    },
                    trailingContent = ListItemContent.Custom {
                        Icon(
                            resourceId = CommonDrawables.ic_compound_delete,
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
                        headlineContent = { Text(text = stringResource(id = R.string.screen_create_poll_add_option_btn)) },
                        leadingContent = ListItemContent.Icon(
                            iconSource = IconSource.Vector(Icons.Default.Add),
                        ),
                        style = ListItemStyle.Primary,
                        onClick = {
                            state.eventSink(CreatePollEvents.AddAnswer)
                            coroutineScope.launch(Dispatchers.Main) {
                                lazyListState.animateScrollToItem(state.answers.size + 1)
                                answerFocusRequester.requestFocus()
                            }
                        },
                    )
                }
            }
            item {
                Column {
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.screen_create_poll_anonymous_headline)) },
                        supportingContent = { Text(text = stringResource(id = R.string.screen_create_poll_anonymous_desc)) },
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

@PreviewsDayNight
@Composable
internal fun CreatePollViewPreview(
    @PreviewParameter(CreatePollStateProvider::class) state: CreatePollState
) = ElementPreview {
    CreatePollView(
        state = state,
    )
}

private val keyboardOptions = KeyboardOptions.Default.copy(
    capitalization = KeyboardCapitalization.Sentences,
    imeAction = ImeAction.Next,
)
