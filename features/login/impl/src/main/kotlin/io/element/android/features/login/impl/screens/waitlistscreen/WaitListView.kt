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

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.error.isWaitListError
import io.element.android.features.login.impl.error.loginError
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.theme.ElementTheme
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

    Box(modifier = modifier) {
        WaitListBackground()
        WaitListContent(state, onCancelClicked)
        WaitListError(state)
    }
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
private fun WaitListBackground(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .background(Color.White)
        )
        Image(
            modifier = Modifier
                .fillMaxWidth(),
            painter = painterResource(id = R.drawable.light_dark),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .background(Color(0xFF121418))
        )
    }
}

@Composable
private fun WaitListContent(
    state: WaitListState,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        if (state.loginAction !is Async.Success) {
            ElementTheme(darkTheme = true) {
                TextButton(
                    title = stringResource(CommonStrings.action_cancel),
                    onClick = onCancelClicked,
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAbsoluteAlignment(
                horizontalBias = 0f,
                verticalBias = -0.05f
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.loginAction.isLoading()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Spacer(modifier = Modifier.height(18.dp))
                val titleRes = when (state.loginAction) {
                    is Async.Success -> R.string.screen_waitlist_title_success
                    else -> R.string.screen_waitlist_title
                }
                Text(
                    text = withColoredPeriod(titleRes),
                    style = ElementTheme.typography.fontHeadingXlBold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(8.dp))
                val subtitle = when (state.loginAction) {
                    is Async.Success -> stringResource(
                        id = R.string.screen_waitlist_message_success,
                        state.appName,
                    )
                    else -> stringResource(
                        id = R.string.screen_waitlist_message,
                        state.appName,
                        state.serverName,
                    )
                }
                Text(
                    modifier = Modifier.widthIn(max = 360.dp),
                    text = subtitle,
                    style = ElementTheme.typography.fontBodyLgRegular,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                )
            }
        }
        if (state.loginAction is Async.Success) {
            ElementTheme(darkTheme = true) {
                Button(
                    title = stringResource(id = CommonStrings.action_continue),
                    onClick = { state.eventSink.invoke(WaitListEvents.Continue) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun withColoredPeriod(
    @StringRes textRes: Int,
) = buildAnnotatedString {
    val text = stringResource(textRes)
    append(text)
    if (text.endsWith(".")) {
        addStyle(
            style = SpanStyle(
                // Light.colorGreen700
                color = Color(0xff0bc491),
            ),
            start = text.length - 1,
            end = text.length,
        )
    }
}

@Preview
@Composable
internal fun WaitListViewLightPreview(@PreviewParameter(WaitListStateProvider::class) state: WaitListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun WaitListViewDarkPreview(@PreviewParameter(WaitListStateProvider::class) state: WaitListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: WaitListState) {
    WaitListView(
        state = state,
        onCancelClicked = {},
    )
}
