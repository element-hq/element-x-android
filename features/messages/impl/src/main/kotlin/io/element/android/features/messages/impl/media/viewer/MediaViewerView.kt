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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.messages.impl.media.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.media.local.LocalMediaView
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun MediaViewerView(
    state: MediaViewerState,
    modifier: Modifier = Modifier,
) {

    fun onRetry() {
        state.eventSink(MediaViewerEvents.RetryLoading)
    }

    Scaffold(modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            when (state.downloadedMedia) {
                is Async.Success -> LocalMediaView(state.downloadedMedia.state)
                is Async.Failure -> ErrorView(stringResource(id = StringR.string.error_unknown), ::onRetry)
                else -> CircularProgressIndicator(
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Composable
private fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = errorMessage)
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = onRetry
        ) {
            Text(text = stringResource(id = StringR.string.action_retry))
        }

    }
}

@Preview
@Composable
fun MediaViewerViewLightPreview(@PreviewParameter(MediaViewerStateProvider::class) state: MediaViewerState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun MediaViewerViewDarkPreview(@PreviewParameter(MediaViewerStateProvider::class) state: MediaViewerState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: MediaViewerState) {
    MediaViewerView(
        state = state,
    )
}
