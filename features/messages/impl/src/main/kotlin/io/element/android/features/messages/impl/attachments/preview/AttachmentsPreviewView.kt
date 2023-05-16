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

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.media.local.LocalMediaView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.R as StringsR

@Composable
fun AttachmentsPreviewView(
    state: AttachmentsPreviewState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {

    fun onSendClicked() {
    }

    Scaffold(modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(
                modifier = Modifier.height(80.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (state.attachment) {
                    is Attachment.Media -> LocalMediaView(
                        localMedia = state.attachment.localMedia
                    )
                }
            }
            AttachmentsPreviewBottomActions(
                onCancelClicked = onDismiss,
                onSendClicked = ::onSendClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 120.dp)
                    .padding(all = 24.dp)
            )
        }
    }
}

@Composable
private fun AttachmentsPreviewBottomActions(
    onCancelClicked: () -> Unit,
    onSendClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onCancelClicked) {
            Text(stringResource(id = StringsR.string.action_cancel))
        }
        TextButton(onClick = onSendClicked) {
            Text(stringResource(id = StringsR.string.action_send))
        }
    }
}

@Preview
@Composable
fun AttachmentsPreviewViewDarkPreview(@PreviewParameter(AttachmentsPreviewStateProvider::class) state: AttachmentsPreviewState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: AttachmentsPreviewState) {
    AttachmentsPreviewView(
        state = state,
        onDismiss = {},
    )
}
