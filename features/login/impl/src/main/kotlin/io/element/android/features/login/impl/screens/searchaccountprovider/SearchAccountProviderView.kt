/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.login.impl.screens.searchaccountprovider

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderView
import io.element.android.features.login.impl.changeserver.ChangeServerEvents
import io.element.android.features.login.impl.changeserver.ChangeServerView
import io.element.android.features.login.impl.resolver.HomeserverData
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.onTabOrEnterKeyFocusNext
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=611-61435
 */
@Composable
fun SearchAccountProviderView(
    state: SearchAccountProviderState,
    onBackClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onClick = onBackClick) }
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
                        iconStyle = BigIcon.Style.Default(CompoundIcons.Search()),
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
                                        imageVector = CompoundIcons.Close(),
                                        contentDescription = stringResource(CommonStrings.action_clear)
                                    )
                                }
                            }
                        } else {
                            null
                        },
                        supportingText = {
                            Text(text = stringResource(id = R.string.screen_account_provider_form_notice), color = MaterialTheme.colorScheme.secondary)
                        }
                    )
                }

                when (state.userInputResult) {
                    is AsyncData.Failure -> {
                        // Ignore errors (let the user type more chars)
                    }
                    is AsyncData.Loading -> {
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
                    is AsyncData.Success -> {
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
                    AsyncData.Uninitialized -> Unit
                }
                item {
                    Spacer(Modifier.height(32.dp))
                }
            }
            ChangeServerView(
                state = state.changeServerState,
                onLearnMoreClick = onLearnMoreClick,
                onSuccess = onSuccess,
            )
        }
    }
}

@Composable
private fun HomeserverData.toAccountProvider(): AccountProvider {
    val isMatrixOrg = homeserverUrl == AuthenticationConfig.MATRIX_ORG_URL
    return AccountProvider(
        url = homeserverUrl,
        subtitle = if (isMatrixOrg) stringResource(id = R.string.screen_change_account_provider_matrix_org_subtitle) else null,
        // There is no need to know for other servers right now
        isPublic = isMatrixOrg,
        isMatrixOrg = isMatrixOrg,
        isValid = isWellknownValid,
    )
}

@PreviewsDayNight
@Composable
internal fun SearchAccountProviderViewPreview(@PreviewParameter(SearchAccountProviderStateProvider::class) state: SearchAccountProviderState) = ElementPreview {
    SearchAccountProviderView(
        state = state,
        onBackClick = {},
        onLearnMoreClick = {},
        onSuccess = {},
    )
}
