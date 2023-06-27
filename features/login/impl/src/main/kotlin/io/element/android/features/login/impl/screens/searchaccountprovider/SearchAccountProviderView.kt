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

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package io.element.android.features.login.impl.screens.searchaccountprovider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderView
import io.element.android.features.login.impl.changeserver.ChangeServerEvents
import io.element.android.features.login.impl.changeserver.ChangeServerView
import io.element.android.features.login.impl.resolver.HomeserverData
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.onTabOrEnterKeyFocusNext
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.R as StringR

/**
 * https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=611-61435
 */
@Composable
fun SearchAccountProviderView(
    state: SearchAccountProviderState,
    onBackPressed: () -> Unit,
    onLearnMoreClicked: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onClick = onBackPressed) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth(), state = rememberLazyListState()) {
                item {
                    IconTitleSubtitleMolecule(
                        modifier = Modifier.padding(top = 16.dp, bottom = 40.dp, start = 16.dp, end = 16.dp),
                        iconImageVector = Icons.Filled.Search,
                        title = stringResource(id = R.string.screen_account_provider_form_title),
                        subTitle = stringResource(id = R.string.screen_account_provider_form_subtitle),
                    )
                }
                item {
                    // TextInput
                    var userInputState by textFieldState(stateValue = state.userInput)
                    val focusManager = LocalFocusManager.current
                    OutlinedTextField(
                        value = userInputState,
                        // readOnly = isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .onTabOrEnterKeyFocusNext(focusManager)
                            .testTag(TestTags.changeServerServer),
                        onValueChange = {
                            userInputState = it
                            eventSink(SearchAccountProviderEvents.UserInput(it))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                        singleLine = true,
                        trailingIcon = if (userInputState.isNotEmpty()) {
                            {
                                IconButton(onClick = {
                                    userInputState = ""
                                    eventSink(SearchAccountProviderEvents.UserInput(""))
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = stringResource(StringR.string.action_clear)
                                    )
                                }
                            }
                        } else null,
                        supportingText = {
                            Text(text = stringResource(id = R.string.screen_account_provider_form_notice), color = MaterialTheme.colorScheme.secondary)
                        }
                    )
                }

                when (state.userInputResult) {
                    is Async.Failure -> {
                        // Ignore errors (let the user type more chars)
                    }
                    is Async.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                    is Async.Success -> {
                        items(state.userInputResult.data) { homeserverData ->
                            val item = homeserverData.toAccountProvider()
                            AccountProviderView(
                                item = item,
                                onClick = {
                                    state.changeServerState.eventSink.invoke(ChangeServerEvents.ChangeServer(item))
                                }
                            )
                        }
                    }
                    Async.Uninitialized -> Unit
                }
                item {
                    Spacer(Modifier.height(32.dp))
                }
            }
            ChangeServerView(
                state = state.changeServerState,
                onLearnMoreClicked = onLearnMoreClicked,
                onDone = onDone,
            )
        }
    }
}

@Composable
private fun HomeserverData.toAccountProvider(): AccountProvider {
    val isMatrixOrg = homeserverUrl == "https://matrix.org"
    return AccountProvider(
        title = homeserverUrl.removePrefix("http://").removePrefix("https://"),
        subtitle = if (isMatrixOrg) stringResource(id = R.string.screen_change_account_provider_matrix_org_subtitle) else null,
        isPublic = isMatrixOrg, // There is no need to know for other servers right now
        isMatrixOrg = isMatrixOrg,
        isValid = isWellknownValid,
        supportSlidingSync = supportSlidingSync,
    )
}

@Preview
@Composable
fun SearchAccountProviderViewLightPreview(@PreviewParameter(SearchAccountProviderStateProvider::class) state: SearchAccountProviderState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun SearchAccountProviderViewDarkPreview(@PreviewParameter(SearchAccountProviderStateProvider::class) state: SearchAccountProviderState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: SearchAccountProviderState) {
    SearchAccountProviderView(
        state = state,
        onBackPressed = {},
        onLearnMoreClicked = {},
        onDone = {},
    )
}
