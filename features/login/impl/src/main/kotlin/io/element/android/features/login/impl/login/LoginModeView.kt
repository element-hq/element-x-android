/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.dialogs.SlidingSyncNotSupportedDialog
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.libraries.androidutils.system.openGooglePlay
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.LocalBuildMeta
import io.element.android.libraries.matrix.api.auth.AuthenticationException
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
    val context = LocalContext.current
    when (loginMode) {
        is AsyncData.Failure -> {
            when (val error = loginMode.error) {
                is ChangeServerError -> {
                    when (error) {
                        ChangeServerError.InvalidServer ->
                            ErrorDialog(
                                content = stringResource(R.string.screen_change_server_error_invalid_homeserver),
                                onSubmit = onClearError,
                            )
                        is ChangeServerError.UnsupportedServer -> {
                            ErrorDialog(
                                content = stringResource(R.string.screen_login_error_unsupported_authentication),
                                onSubmit = onClearError,
                            )
                        }
                        is ChangeServerError.Error -> {
                            ErrorDialog(
                                content = error.messageStr ?: stringResource(CommonStrings.error_unknown),
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
                        is ChangeServerError.NeedElementPro -> {
                            ConfirmationDialog(
                                title = stringResource(R.string.screen_change_server_error_element_pro_required_title),
                                content = stringResource(
                                    R.string.screen_change_server_error_element_pro_required_message,
                                    error.unauthorisedAccountProviderTitle,
                                ),
                                submitText = stringResource(R.string.screen_change_server_error_element_pro_required_action_android),
                                onSubmitClick = {
                                    context.openGooglePlay(error.applicationId)
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
                is AuthenticationException.AccountAlreadyLoggedIn -> {
                    ErrorDialog(
                        content = stringResource(CommonStrings.error_account_already_logged_in, error.userId),
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

@PreviewsDayNight
@Composable
internal fun LoginModeViewPreview(@PreviewParameter(LoginModeViewErrorProvider::class) error: Throwable) {
    ElementPreview {
        LoginModeView(
            loginMode = AsyncData.Failure(error),
            onClearError = {},
            onLearnMoreClick = {},
            onOidcDetails = {},
            onNeedLoginPassword = {},
            onCreateAccountContinue = {}
        )
    }
}
