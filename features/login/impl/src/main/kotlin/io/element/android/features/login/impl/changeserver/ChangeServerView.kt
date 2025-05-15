/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.changeserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.dialogs.SlidingSyncNotSupportedDialog
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.LocalBuildMeta

@Composable
fun ChangeServerView(
    state: ChangeServerState,
    onLearnMoreClick: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink
    when (state.changeServerAction) {
        is AsyncData.Failure -> {
            when (val error = state.changeServerAction.error as? ChangeServerError) {
                is ChangeServerError.Error -> {
                    ErrorDialog(
                        modifier = modifier,
                        content = error.message(),
                        onSubmit = {
                            eventSink.invoke(ChangeServerEvents.ClearError)
                        }
                    )
                }
                is ChangeServerError.SlidingSyncAlert -> {
                    SlidingSyncNotSupportedDialog(
                        modifier = modifier,
                        onLearnMoreClick = {
                            onLearnMoreClick()
                            eventSink.invoke(ChangeServerEvents.ClearError)
                        },
                        onDismiss = {
                            eventSink.invoke(ChangeServerEvents.ClearError)
                        }
                    )
                }
                is ChangeServerError.UnauthorizedAccountProvider -> {
                    ErrorDialog(
                        modifier = modifier,
                        content = stringResource(
                            id = R.string.screen_change_server_error_unauthorized_homeserver,
                            LocalBuildMeta.current.applicationName,
                            error.unauthorisedAccountProviderTitle,
                        ),
                        onSubmit = {
                            eventSink.invoke(ChangeServerEvents.ClearError)
                        }
                    )
                }
                null -> Unit
            }
        }
        is AsyncData.Loading -> ProgressDialog()
        is AsyncData.Success -> {
            val latestOnSuccess by rememberUpdatedState(onSuccess)
            LaunchedEffect(state.changeServerAction) {
                latestOnSuccess()
            }
        }
        AsyncData.Uninitialized -> Unit
    }
}

@PreviewsDayNight
@Composable
internal fun ChangeServerViewPreview(@PreviewParameter(ChangeServerStateProvider::class) state: ChangeServerState) = ElementPreview {
    ChangeServerView(
        state = state,
        onLearnMoreClick = {},
        onSuccess = {},
    )
}
