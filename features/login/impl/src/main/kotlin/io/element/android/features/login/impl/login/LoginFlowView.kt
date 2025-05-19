/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.login.impl.dialogs.SlidingSyncNotSupportedDialog
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.features.login.impl.screens.confirmaccountprovider.LoginFlow
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LoginFlowView(
    loginFlow: AsyncData<LoginFlow>,
    onClearError: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onOidcDetails: (OidcDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onCreateAccountContinue: (url: String) -> Unit
) {
    when (loginFlow) {
        is AsyncData.Failure -> {
            when (val error = loginFlow.error) {
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
                        onDismiss = {
                            onClearError()
                        }
                    )
                }
                is AccountCreationNotSupported -> {
                    ErrorDialog(
                        content = stringResource(CommonStrings.error_account_creation_not_possible),
                        onSubmit = onClearError,
                    )
                }
            }
        }
        is AsyncData.Loading -> Unit // The Continue button shows the loading state
        is AsyncData.Success -> {
            when (val loginFlowState = loginFlow.data) {
                is LoginFlow.OidcFlow -> onOidcDetails(loginFlowState.oidcDetails)
                LoginFlow.PasswordLogin -> onNeedLoginPassword()
                is LoginFlow.AccountCreationFlow -> onCreateAccountContinue(loginFlowState.url)
            }
        }
        AsyncData.Uninitialized -> Unit
    }
}
