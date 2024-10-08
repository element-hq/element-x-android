/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.error.sendAttachmentError
import io.element.android.libraries.designsystem.atomic.molecules.ButtonRowMolecule
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.ProgressDialogType
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.mediaviewer.api.local.LocalMediaView
import io.element.android.libraries.mediaviewer.api.local.rememberLocalMediaViewState
import io.element.android.libraries.ui.strings.CommonStrings
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState

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

    if (state.sendActionState is SendActionState.Done) {
        val latestOnDismiss by rememberUpdatedState(onDismiss)
        LaunchedEffect(state.sendActionState) {
            latestOnDismiss()
        }
    }

    Scaffold(modifier) {
        AttachmentPreviewContent(
            attachment = state.attachment,
            onSendClick = ::postSendAttachment,
            onDismiss = onDismiss
        )
    }
    AttachmentSendStateView(
        sendActionState = state.sendActionState,
        onDismissClick = ::postClearSendState,
        onRetryClick = ::postSendAttachment
    )
}

@Composable
private fun AttachmentSendStateView(
    sendActionState: SendActionState,
    onDismissClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    when (sendActionState) {
        is SendActionState.Sending -> {
            ProgressDialog(
                type = when (sendActionState) {
                    is SendActionState.Sending.Uploading -> ProgressDialogType.Determinate(sendActionState.progress)
                    SendActionState.Sending.Processing -> ProgressDialogType.Indeterminate
                },
                text = stringResource(id = CommonStrings.common_sending),
                showCancelButton = true,
                onDismissRequest = onDismissClick,
            )
        }
        is SendActionState.Failure -> {
            RetryDialog(
                content = stringResource(sendAttachmentError(sendActionState.error)),
                onDismiss = onDismissClick,
                onRetry = onRetryClick
            )
        }
        else -> Unit
    }
}

@Composable
private fun AttachmentPreviewContent(
    attachment: Attachment,
    onSendClick: () -> Unit,
    onDismiss: () -> Unit,
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
        AttachmentsPreviewBottomActions(
            onCancelClick = onDismiss,
            onSendClick = onSendClick,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 24.dp)
                .defaultMinSize(minHeight = 80.dp)
        )
    }
}

@Composable
private fun AttachmentsPreviewBottomActions(
    onCancelClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonRowMolecule(modifier = modifier) {
        TextButton(stringResource(id = CommonStrings.action_cancel), onClick = onCancelClick)
        TextButton(stringResource(id = CommonStrings.action_send), onClick = onSendClick)
    }
}

// Only preview in dark, dark theme is forced on the Node.
@Preview
@Composable
internal fun AttachmentsPreviewViewPreview(@PreviewParameter(AttachmentsPreviewStateProvider::class) state: AttachmentsPreviewState) = ElementPreviewDark {
    AttachmentsPreviewView(
        state = state,
        onDismiss = {},
    )
}
