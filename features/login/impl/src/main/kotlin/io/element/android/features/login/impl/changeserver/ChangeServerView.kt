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

package io.element.android.features.login.impl.changeserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.login.impl.dialogs.SlidingSyncNotSupportedDialog
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight

@Composable
fun ChangeServerView(
    state: ChangeServerState,
    onLearnMoreClicked: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink
    when (state.changeServerAction) {
        is Async.Failure -> {
            when (val error = state.changeServerAction.exception) {
                is ChangeServerError.Error -> {
                    ErrorDialog(
                        modifier = modifier,
                        content = error.message(),
                        onDismiss = {
                            eventSink.invoke(ChangeServerEvents.ClearError)
                        }
                    )
                }
                is ChangeServerError.SlidingSyncAlert -> {
                    SlidingSyncNotSupportedDialog(
                        modifier = modifier,
                        onLearnMoreClicked = {
                            onLearnMoreClicked()
                            eventSink.invoke(ChangeServerEvents.ClearError)
                        }, onDismiss = {
                        eventSink.invoke(ChangeServerEvents.ClearError)
                    })
                }
            }
        }
        is Async.Loading -> ProgressDialog()
        is Async.Success -> LaunchedEffect(state.changeServerAction) {
            onDone()
        }
        Async.Uninitialized -> Unit
    }
}

@Preview
@Composable
fun ChangeServerViewLightPreview(@PreviewParameter(ChangeServerStateProvider::class) state: ChangeServerState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun ChangeServerViewDarkPreview(@PreviewParameter(ChangeServerStateProvider::class) state: ChangeServerState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ChangeServerState) {
    ChangeServerView(
        state = state,
        onLearnMoreClicked = {},
        onDone = {},
    )
}
