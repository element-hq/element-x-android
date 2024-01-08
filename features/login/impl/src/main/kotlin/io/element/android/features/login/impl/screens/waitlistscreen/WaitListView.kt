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

package io.element.android.features.login.impl.screens.waitlistscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.error.isWaitListError
import io.element.android.features.login.impl.error.loginError
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.pages.SunsetPage
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.ui.strings.CommonStrings

// Ref: https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?type=design&node-id=6761-148425
// Only the first screen can be displayed, since once logged in, this Node will be remove by the RootNode.
@Composable
fun WaitListView(
    state: WaitListState,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> state.eventSink.invoke(WaitListEvents.AttemptLogin)
            else -> Unit
        }
    }
    WaitListContent(state, onCancelClicked, modifier)
}

@Composable
private fun WaitListError(state: WaitListState) {
    // Display a dialog for error other than the waitlist error
    state.loginAction.errorOrNull()?.let { error ->
        if (error.isWaitListError().not()) {
            RetryDialog(
                content = stringResource(id = loginError(error)),
                onRetry = {
                    state.eventSink.invoke(WaitListEvents.AttemptLogin)
                },
                onDismiss = {
                    state.eventSink.invoke(WaitListEvents.ClearError)
                }
            )
        }
    }
}

@Composable
private fun WaitListContent(
    state: WaitListState,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        val title = stringResource(
            when (state.loginAction) {
                is AsyncData.Success -> R.string.screen_waitlist_title_success
                else -> R.string.screen_waitlist_title
            }
        )
        val subtitle = when (state.loginAction) {
            is AsyncData.Success -> stringResource(
                id = R.string.screen_waitlist_message_success,
                state.appName,
            )
            else -> stringResource(
                id = R.string.screen_waitlist_message,
                state.appName,
                state.serverName,
            )
        }
        SunsetPage(
            isLoading = state.loginAction.isLoading(),
            title = title,
            subtitle = subtitle,
        ) {
            OverallContent(state, onCancelClicked)
        }
        WaitListError(state)
    }
}

@Composable
private fun OverallContent(
    state: WaitListState,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (state.loginAction !is AsyncData.Success) {
            CompositionLocalProvider(LocalContentColor provides Color.Black) {
                TextButton(
                    text = stringResource(CommonStrings.action_cancel),
                    onClick = onCancelClicked,
                )
            }
        }
        if (state.loginAction is AsyncData.Success) {
            Button(
                text = stringResource(id = CommonStrings.action_continue),
                onClick = { state.eventSink.invoke(WaitListEvents.Continue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
            )
        }

    }
}

@PreviewsDayNight
@Composable
internal fun WaitListViewPreview(@PreviewParameter(WaitListStateProvider::class) state: WaitListState) = ElementPreview {
    WaitListView(
        state = state,
        onCancelClicked = {},
    )
}
