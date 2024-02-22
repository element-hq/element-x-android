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

package io.element.android.features.preferences.impl.blockedusers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.async.AsyncIndicator
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorHost
import io.element.android.libraries.designsystem.components.async.rememberAsyncIndicatorState
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersView(
    state: BlockedUsersState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(CommonStrings.common_blocked_users),
                            style = ElementTheme.typography.aliasScreenTitle,
                        )
                    },
                    navigationIcon = {
                        BackButton(onClick = onBackPressed)
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                items(state.blockedUsers) { userId ->
                    BlockedUserItem(
                        userId = userId,
                        onClick = { state.eventSink(BlockedUsersEvents.Unblock(it)) }
                    )
                }
            }
        }

        val asyncIndicatorState = rememberAsyncIndicatorState()
        AsyncIndicatorHost(modifier = Modifier.statusBarsPadding(), state = asyncIndicatorState)

        when (state.unblockUserAction) {
            is AsyncAction.Loading -> {
                LaunchedEffect(state.unblockUserAction) {
                    asyncIndicatorState.enqueue {
                        AsyncIndicator.Loading(text = stringResource(R.string.screen_blocked_users_unblocking))
                    }
                }
            }
            is AsyncAction.Failure -> {
                LaunchedEffect(state.unblockUserAction) {
                    asyncIndicatorState.enqueue(durationMs = AsyncIndicator.DURATION_SHORT) {
                        AsyncIndicator.Failure(text = stringResource(CommonStrings.common_failed))
                    }
                }
            }
            is AsyncAction.Confirming -> {
                ConfirmationDialog(
                    title = stringResource(R.string.screen_blocked_users_unblock_alert_title),
                    content = stringResource(R.string.screen_blocked_users_unblock_alert_description),
                    submitText = stringResource(R.string.screen_blocked_users_unblock_alert_action),
                    onSubmitClicked = { state.eventSink(BlockedUsersEvents.ConfirmUnblock) },
                    onDismiss = { state.eventSink(BlockedUsersEvents.Cancel) }
                )
            }
            else -> Unit
        }
    }
}

@Composable
private fun BlockedUserItem(
    userId: UserId,
    onClick: (UserId) -> Unit,
) {
    MatrixUserRow(
        modifier = Modifier.clickable { onClick(userId) },
        matrixUser = MatrixUser(userId),
    )
}

@PreviewsDayNight
@Composable
internal fun BlockedUsersViewPreview(@PreviewParameter(BlockedUsersStatePreviewProvider::class) state: BlockedUsersState) {
    ElementPreview {
        BlockedUsersView(
            state = state,
            onBackPressed = {}
        )
    }
}
