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

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.messagecomposer.AttachmentsState
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.mediaviewer.api.local.LocalMediaView
import io.element.android.libraries.mediaviewer.api.local.rememberLocalMediaViewState
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState

@Composable
fun AttachmentsPreviewView(
    state: AttachmentsState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is AttachmentsState.Previewing -> {
            Scaffold(modifier,
                floatingActionButton = {
                    FloatingActionButton(onClick = onDismiss) { Icon(imageVector = CompoundIcons.Close(), contentDescription = null) }
                },
                floatingActionButtonPosition = FabPosition.Start
            ) {
                AttachmentPreviewContent(
                    attachment = state.attachments.first(),
                )
            }
        }
        else -> Unit
    }
}

@Composable
private fun AttachmentPreviewContent(
    attachment: Attachment,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (attachment) {
                is Attachment.Media -> {
                    val localMediaViewState = rememberLocalMediaViewState(
                        zoomableState = rememberZoomableState(
                            zoomSpec = ZoomSpec(maxZoomFactor = 4f, preventOverOrUnderZoom = false)
                        )
                    )
                    LocalMediaView(
                        modifier = Modifier.fillMaxSize(),
                        localMedia = attachment.localMedia,
                        localMediaViewState = localMediaViewState,
                        onClick = {}
                    )
                }
            }
        }
    }
}

// Only preview in dark, dark theme is forced on the Node.
@Preview
@Composable
internal fun AttachmentsPreviewViewPreview(@PreviewParameter(AttachmentsPreviewStateProvider::class) state: AttachmentsState) = ElementPreviewDark {
    AttachmentsPreviewView(
        state = state,
        onDismiss = {},
    )
}
