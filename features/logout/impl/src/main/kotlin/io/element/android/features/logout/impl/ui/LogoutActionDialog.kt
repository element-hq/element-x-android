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

package io.element.android.features.logout.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import io.element.android.features.logout.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LogoutActionDialog(
    state: AsyncAction<String?>,
    onConfirmClicked: () -> Unit,
    onForceLogoutClicked: () -> Unit,
    onDismissError: () -> Unit, // TODO Rename
    onSuccessLogout: (String?) -> Unit,
) {
    when (state) {
        AsyncAction.Uninitialized ->
            Unit
        AsyncAction.Confirming ->
            LogoutConfirmationDialog(
                onSubmitClicked = onConfirmClicked,
                onDismiss = onDismissError
            )
        is AsyncAction.Loading ->
            ProgressDialog(text = stringResource(id = R.string.screen_signout_in_progress_dialog_content))
        is AsyncAction.Failure ->
            RetryDialog(
                title = stringResource(id = CommonStrings.dialog_title_error),
                content = stringResource(id = CommonStrings.error_unknown),
                retryText = stringResource(id = CommonStrings.action_signout_anyway),
                onRetry = onForceLogoutClicked,
                onDismiss = onDismissError,
            )
        is AsyncAction.Success ->
            LaunchedEffect(state) {
                onSuccessLogout(state.data)
            }
    }
}
