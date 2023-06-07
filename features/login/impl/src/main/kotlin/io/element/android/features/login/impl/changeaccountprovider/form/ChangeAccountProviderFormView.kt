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

package io.element.android.features.login.impl.changeaccountprovider.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.accountprovider.item.AccountProvider
import io.element.android.features.login.impl.accountprovider.item.AccountProviderView
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

/**
 * https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=611-61435
 */
@Composable
fun ChangeAccountProviderFormView(
    state: ChangeAccountProviderFormState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onAccountProviderClicked: (AccountProvider) -> Unit = {},
) {
    val eventSink = state.eventSink
    val scrollState = rememberScrollState()

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
            Column(
                modifier = Modifier
                    .verticalScroll(
                        state = scrollState,
                    )
            ) {
                IconTitleSubtitleMolecule(
                    modifier = Modifier.padding(top = 16.dp, bottom = 40.dp, start = 16.dp, end = 16.dp),
                    iconImageVector = Icons.Filled.Home,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = stringResource(id = R.string.screen_account_provider_form_title),
                    subTitle = stringResource(id = R.string.screen_account_provider_form_subtitle),
                )

                // TextInput
                var userInputState by textFieldState(stateValue = state.userInput)

                OutlinedTextField(
                    value = userInputState,
                    // readOnly = isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 30.dp),
                    onValueChange = {
                        userInputState = it
                        eventSink(ChangeAccountProviderFormEvents.UserInput(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done,
                    ),
                    singleLine = true,
                    maxLines = 1,
                    trailingIcon = if (userInputState.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                userInputState = ""
                                eventSink(ChangeAccountProviderFormEvents.UserInput(""))
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(io.element.android.libraries.ui.strings.R.string.action_clear)
                                )
                            }
                        }
                    } else null,
                    supportingText = {
                        Text(text = stringResource(id = R.string.screen_account_provider_form_notice), color = MaterialTheme.colorScheme.secondary)
                    }
                )

                when (state.userInputResult) {
                    is Async.Failure -> {
                        // Ignore errors (let the user type more chars)
                    }
                    is Async.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    is Async.Success -> {
                        state.userInputResult.state.forEach { homeserverData ->
                            val isMatrixOrg = homeserverData.homeserverUrl == "https://matrix.org"
                            val item = AccountProvider(
                                title = homeserverData.homeserverUrl.removePrefix("http://").removePrefix("https://"),
                                subtitle = if (isMatrixOrg) stringResource(id = R.string.screen_change_account_provider_matrix_org_subtitle) else null,
                                isPublic = isMatrixOrg, // There is no need to know for other servers right now
                                isMatrixOrg = isMatrixOrg,
                            )
                            AccountProviderView(
                                item = item,
                                onClick = {
                                    onAccountProviderClicked(item)
                                }
                            )
                        }
                    }
                    Async.Uninitialized -> Unit
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Preview
@Composable
fun ChangeAccountProviderFormViewLightPreview(@PreviewParameter(ChangeAccountProviderStateFormProvider::class) state: ChangeAccountProviderFormState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun ChangeAccountProviderFormViewDarkPreview(@PreviewParameter(ChangeAccountProviderStateFormProvider::class) state: ChangeAccountProviderFormState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ChangeAccountProviderFormState) {
    ChangeAccountProviderFormView(
        state = state,
    )
}
