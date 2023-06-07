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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.login.impl.R
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.ui.strings.R as StringR

@Composable
internal fun SlidingSyncNotSupportedDialog(onLearnMoreClicked: () -> Unit, onDismiss: () -> Unit) {
    ConfirmationDialog(
        onDismiss = onDismiss,
        submitText = stringResource(StringR.string.action_learn_more),
        onSubmitClicked = onLearnMoreClicked,
        onCancelClicked = onDismiss,
        emphasizeSubmitButton = true,
        title = stringResource(StringR.string.dialog_title_error),
        content = stringResource(R.string.screen_change_server_error_no_sliding_sync_message),
    )
}
