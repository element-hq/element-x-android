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

package io.element.android.features.login.impl.accountprovider

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.button.ButtonWithProgress
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag

@Composable
fun AccountProviderView(
    state: AccountProviderState,
    modifier: Modifier = Modifier,
    onOidcDetails: (OidcDetails) -> Unit = {},
    onLoginPasswordNeeded: () -> Unit = {},
    onLearnMoreClicked: () -> Unit = {},
    onChange: () -> Unit = {},
) {
    val isLoading by remember(state.loginFlow) {
        derivedStateOf {
            state.loginFlow is Async.Loading
        }
    }
    val eventSink = state.eventSink

    HeaderFooterPage(
        modifier = modifier,
        header = {
            IconTitleSubtitleMolecule(
                modifier = Modifier.padding(top = 60.dp),
                iconImageVector = Icons.Filled.AccountCircle,
                title = stringResource(
                    id = if (state.isAccountCreation) {
                        R.string.screen_account_provider_signup_title
                    } else {
                        R.string.screen_account_provider_signin_title
                    },
                    state.accountProvider.title
                ),
                subTitle = stringResource(
                    id = if (state.isAccountCreation) {
                        R.string.screen_account_provider_signup_subtitle
                    } else {
                        // Use same value for now.
                        R.string.screen_account_provider_signup_subtitle
                    },
                )
            )
        },
        footer = {
            ButtonColumnMolecule {
                ButtonWithProgress(
                    text = stringResource(id = R.string.screen_account_provider_continue),
                    showProgress = isLoading,
                    onClick = { eventSink.invoke(AccountProviderEvents.Continue) },
                    enabled = state.submitEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.changeServerContinue)
                )
                TextButton(
                    onClick = {
                        onChange()
                    },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.screen_account_provider_change))
                }
            }
        }
    ) {
        when (state.loginFlow) {
            is Async.Failure -> {
                when (val error = state.loginFlow.error) {
                    is ChangeServerError.InlineErrorMessage -> {
                        ErrorDialog(
                            content = error.message(),
                            onDismiss = {
                                eventSink.invoke(AccountProviderEvents.ClearError)
                            }
                        )
                    }
                    is ChangeServerError.SlidingSyncAlert -> {
                        SlidingSyncNotSupportedDialog(onLearnMoreClicked = {
                            onLearnMoreClicked()
                            eventSink(AccountProviderEvents.ClearError)
                        }, onDismiss = {
                            eventSink(AccountProviderEvents.ClearError)
                        })
                    }
                }
            }
            is Async.Loading -> Unit // The Continue button shows the loading state
            is Async.Success -> {
                when (val loginFlowState = state.loginFlow.state) {
                    is LoginFlow.OidcFlow -> onOidcDetails(loginFlowState.oidcDetails)
                    LoginFlow.PasswordLogin -> onLoginPasswordNeeded()
                }
            }
            Async.Uninitialized -> Unit
        }
    }
}

@Preview
@Composable
fun AccountProviderViewLightPreview(@PreviewParameter(AccountProviderStateProvider::class) state: AccountProviderState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun AccountProviderViewDarkPreview(@PreviewParameter(AccountProviderStateProvider::class) state: AccountProviderState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: AccountProviderState) {
    AccountProviderView(
        state = state,
    )
}
