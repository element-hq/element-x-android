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

package io.element.android.appnav.loggedin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.exception.isNetworkError
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LoggedInView(
    state: LoggedInState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        SyncStateView(
            modifier = Modifier.align(Alignment.TopCenter),
            isVisible = state.showSyncSpinner,
        )
    }
    when (state.pusherRegistrationState) {
        is AsyncData.Uninitialized,
        is AsyncData.Loading,
        is AsyncData.Success -> Unit
        is AsyncData.Failure -> {
            state.pusherRegistrationState.errorOrNull()
                ?.getReason()
                ?.let { reason ->
                    ErrorDialog(
                        content = stringResource(id = CommonStrings.common_error_registering_pusher_android, reason),
                        onDismiss = { state.eventSink(LoggedInEvents.CloseErrorDialog) },
                    )
                }
        }
    }
}

private fun Throwable.getReason(): String? {
    return when (this) {
        is PusherRegistrationFailure.RegistrationFailure -> {
            if (isRegisteringAgain && clientException.isNetworkError()) {
                // When registering again, ignore network error
                null
            } else {
                clientException.message ?: "Unknown error"
            }
        }
        is PusherRegistrationFailure.AccountNotVerified -> null
        is PusherRegistrationFailure.NoDistributorsAvailable -> "No distributors available"
        is PusherRegistrationFailure.NoProvidersAvailable -> "No providers available"
        else -> "Other error"
    }
}

@PreviewsDayNight
@Composable
internal fun LoggedInViewPreview(@PreviewParameter(LoggedInStateProvider::class) state: LoggedInState) = ElementPreview {
    LoggedInView(
        state = state
    )
}
