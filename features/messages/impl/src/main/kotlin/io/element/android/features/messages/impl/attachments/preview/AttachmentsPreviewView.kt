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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.error.sendAttachmentError
import io.element.android.features.messages.impl.media.local.LocalMediaView
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.atomic.molecules.ButtonRowMolecule
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.R
import io.element.android.libraries.ui.strings.R as StringsR

@Composable
fun AttachmentsPreviewView(
    state: AttachmentsPreviewState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {

    fun postSendAttachment() {
        state.eventSink(AttachmentsPreviewEvents.SendAttachment)
    }

    fun postClearSendState() {
        state.eventSink(AttachmentsPreviewEvents.ClearSendState)
    }

    if (state.sendActionState is Async.Success) {
        LaunchedEffect(state.sendActionState) {
            onDismiss()
        }
    }

    Scaffold(modifier) {
        Box(
            modifier = Modifier.padding(it),
            contentAlignment = Alignment.Center
        ) {
            AttachmentPreviewContent(
                attachment = state.attachment,
                onSendClicked = ::postSendAttachment,
                onDismiss = onDismiss
            )
        }
    }
    AttachmentSendStateView(
        sendActionState = state.sendActionState,
        onRetryClicked = ::postSendAttachment,
        onRetryDismissed = ::postClearSendState
    )
}

@Composable
private fun AttachmentSendStateView(
    sendActionState: Async<Unit>,
    onRetryDismissed: () -> Unit,
    onRetryClicked: () -> Unit
) {
    when (sendActionState) {
        is Async.Loading -> {
            ProgressDialog(text = stringResource(id = R.string.common_loading))
        }

        is Async.Failure -> {
            RetryDialog(
                content = stringResource(sendAttachmentError(sendActionState.exception)),
                onDismiss = onRetryDismissed,
                onRetry = onRetryClicked
            )
        }
        else -> Unit
    }
}

@Composable
private fun AttachmentPreviewContent(
    attachment: Attachment,
    onSendClicked: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when (attachment) {
                is Attachment.Media -> LocalMediaView(
                    localMedia = attachment.localMedia
                )
            }
        }
        AttachmentsPreviewBottomActions(
            onCancelClicked = onDismiss,
            onSendClicked = onSendClicked,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 120.dp)
                .padding(all = 24.dp)
        )
    }
}

@Composable
private fun AttachmentsPreviewBottomActions(
    onCancelClicked: () -> Unit,
    onSendClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonRowMolecule(
        modifier = modifier,
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
