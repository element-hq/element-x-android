/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.userprofile.shared.blockuser

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.userprofile.shared.R
import io.element.android.features.userprofile.shared.UserProfileEvents
import io.element.android.features.userprofile.shared.UserProfileState
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog

@Composable
fun BlockUserDialogs(state: UserProfileState) {
    when (state.displayConfirmationDialog) {
        null -> Unit
        UserProfileState.ConfirmationDialog.Block -> {
            BlockConfirmationDialog(
                onBlockAction = {
                    state.eventSink(
                        UserProfileEvents.BlockUser(
                            needsConfirmation = false
                        )
                    )
                },
                onDismiss = { state.eventSink(UserProfileEvents.ClearConfirmationDialog) }
            )
        }
        UserProfileState.ConfirmationDialog.Unblock -> {
            UnblockConfirmationDialog(
                onUnblockAction = {
                    state.eventSink(
                        UserProfileEvents.UnblockUser(
                            needsConfirmation = false
                        )
                    )
                },
                onDismiss = { state.eventSink(UserProfileEvents.ClearConfirmationDialog) }
            )
        }
    }
}

@Composable
private fun BlockConfirmationDialog(
    onBlockAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.screen_dm_details_block_user),
        content = stringResource(R.string.screen_dm_details_block_alert_description),
        submitText = stringResource(R.string.screen_dm_details_block_alert_action),
        onSubmitClicked = onBlockAction,
        onDismiss = onDismiss
    )
}

@Composable
private fun UnblockConfirmationDialog(
    onUnblockAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.screen_dm_details_unblock_user),
        content = stringResource(R.string.screen_dm_details_unblock_alert_description),
        submitText = stringResource(R.string.screen_dm_details_unblock_alert_action),
        onSubmitClicked = onUnblockAction,
        onDismiss = onDismiss
    )
}
