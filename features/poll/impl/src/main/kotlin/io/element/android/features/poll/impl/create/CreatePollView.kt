/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.poll.impl.R
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CreatePollView(
    state: CreatePollState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    val navBack = { state.eventSink(CreatePollEvents.ConfirmNavBack) }
    BackHandler(onBack = navBack)
    if (state.showBackConfirmation) {
        ConfirmationDialog(
            content = stringResource(id = R.string.screen_create_poll_cancel_confirmation_content_android),
            onSubmitClick = { state.eventSink(CreatePollEvents.NavBack) },
            onDismiss = { state.eventSink(CreatePollEvents.HideConfirmation) }
        )
    }
    if (state.showDeleteConfirmation) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_edit_poll_delete_confirmation_title),
            content = stringResource(id = R.string.screen_edit_poll_delete_confirmation),
            onSubmitClick = { state.eventSink(CreatePollEvents.Delete(confirmed = true)) },
            onDismiss = { state.eventSink(CreatePollEvents.HideConfirmation) }
        )
    }
    val questionFocusRequester = remember { FocusRequester() }
    val answerFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        questionFocusRequester.requestFocus()
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            CreatePollTopAppBar(
                mode = state.mode,
                saveEnabled = state.canSave,
                onBackClick = navBack,
                onSaveClick = { state.eventSink(CreatePollEvents.Save) }
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
                    ListItem(
                        headlineContent = {
                            TextField(
                                label = stringResource(id = R.string.screen_create_poll_question_desc),
                                value = state.question,
                                onValueChange = {
                                    state.eventSink(CreatePollEvents.SetQuestion(it))
                                },
                                modifier = Modifier
                                    .focusRequester(questionFocusRequester)
                                    .fillMaxWidth(),
                                placeholder = stringResource(id = R.string.screen_create_poll_question_hint),
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
                        TextField(
                            value = answer.text,
                            onValueChange = {
                                state.eventSink(CreatePollEvents.SetAnswer(index, it))
                            },
                            modifier = Modifier
                                .then(if (isLastItem) Modifier.focusRequester(answerFocusRequester) else Modifier)
                                .fillMaxWidth(),
                            placeholder = stringResource(id = R.string.screen_create_poll_answer_hint, index + 1),
                            keyboardOptions = keyboardOptions,
                        )
                    },
                    trailingContent = ListItemContent.Custom {
                        Icon(
                            imageVector = CompoundIcons.Delete(),
                            contentDescription = stringResource(R.string.screen_create_poll_delete_option_a11y, answer.text),
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
                            iconSource = IconSource.Vector(CompoundIcons.Plus()),
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
                        ),
                        onClick = {
                            state.eventSink(
                                CreatePollEvents.SetPollKind(
                                    if (state.pollKind == PollKind.Disclosed) PollKind.Undisclosed else PollKind.Disclosed
                                )
                            )
                        },
                    )
                    if (state.canDelete) {
                        ListItem(
                            headlineContent = { Text(text = stringResource(id = CommonStrings.action_delete_poll)) },
                            style = ListItemStyle.Destructive,
                            onClick = { state.eventSink(CreatePollEvents.Delete(confirmed = false)) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePollTopAppBar(
    mode: CreatePollState.Mode,
    saveEnabled: Boolean,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = when (mode) {
                    CreatePollState.Mode.New -> stringResource(id = R.string.screen_create_poll_title)
                    CreatePollState.Mode.Edit -> stringResource(id = R.string.screen_edit_poll_title)
                },
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        actions = {
            TextButton(
                text = when (mode) {
                    CreatePollState.Mode.New -> stringResource(id = CommonStrings.action_create)
                    CreatePollState.Mode.Edit -> stringResource(id = CommonStrings.action_done)
                },
                onClick = onSaveClick,
                enabled = saveEnabled,
            )
        }
    )
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
