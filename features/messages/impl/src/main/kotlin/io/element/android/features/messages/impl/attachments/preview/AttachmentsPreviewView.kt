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

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.media.local.LocalMediaView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Scaffold

@Composable
fun AttachmentsPreviewView(
    state: AttachmentsPreviewState,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            when (state.attachment) {
                is Attachment.Media -> LocalMediaView(localMedia = state.attachment.localMedia)
            }
        }
    }
}

@Preview
@Composable
fun AttachmentsPreviewViewLightPreview(@PreviewParameter(AttachmentsPreviewStateProvider::class) state: AttachmentsPreviewState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun AttachmentsPreviewViewDarkPreview(@PreviewParameter(AttachmentsPreviewStateProvider::class) state: AttachmentsPreviewState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: AttachmentsPreviewState) {
    AttachmentsPreviewView(
        state = state,
    )
}
