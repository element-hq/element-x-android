/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.dialogs.SlidingSyncNotSupportedDialog
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.theme.LocalBuildMeta
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LoginModeView(
    loginMode: AsyncData<LoginMode>,
    onClearError: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onOidcDetails: (OidcDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onCreateAccountContinue: (url: String) -> Unit
) {
    when (loginMode) {
        is AsyncData.Failure -> {
            when (val error = loginMode.error) {
                is ChangeServerError -> {
                    when (error) {
                        is ChangeServerError.Error -> {
                            ErrorDialog(
                                content = error.message(),
                                onSubmit = onClearError,
                            )
                        }
                        is ChangeServerError.SlidingSyncAlert -> {
                            SlidingSyncNotSupportedDialog(
                                onLearnMoreClick = {
                                    onLearnMoreClick()
                                    onClearError()
                                },
                                onDismiss = onClearError,
                            )
                        }
                        is ChangeServerError.UnauthorizedAccountProvider -> {
                            ErrorDialog(
                                content = stringResource(
                                    id = R.string.screen_change_server_error_unauthorized_homeserver,
                                    LocalBuildMeta.current.applicationName,
                                    error.unauthorisedAccountProviderTitle,
                                ),
                                onSubmit = onClearError,
                            )
                        }
                    }
                }
                is AccountCreationNotSupported -> {
                    ErrorDialog(
                        content = stringResource(CommonStrings.error_account_creation_not_possible),
                        onSubmit = onClearError,
                    )
                }
                else -> {
                    ErrorDialog(
                        content = stringResource(CommonStrings.error_unknown),
                        onSubmit = onClearError,
                    )
                }
            }
        }
        is AsyncData.Loading -> Unit // The Continue button shows the loading state
        is AsyncData.Success -> {
            when (val loginModeData = loginMode.data) {
                is LoginMode.Oidc -> onOidcDetails(loginModeData.oidcDetails)
                LoginMode.PasswordLogin -> onNeedLoginPassword()
                is LoginMode.AccountCreation -> onCreateAccountContinue(loginModeData.url)
            }
            // Also clear the data, to let the next screen be able to go back
            onClearError()
        }
        AsyncData.Uninitialized -> Unit
    }
}
