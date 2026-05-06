/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.mediapreview.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.ProgressDialogType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ListDialog
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.modifiers.niceClickable
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Switch
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaRenderer
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.textcomposer.TextComposer
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.formatter.rememberFileSizeFormatter
import io.element.android.features.mediapreview.impl.R as MediaPreviewR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPreviewView(
    state: MediaPreviewState,
    localMediaRenderer: LocalMediaRenderer,
    modifier: Modifier = Modifier,
) {
    val isUploading = state.sendActionState is SendActionState.Sending.Uploading
    val isDone = state.sendActionState is SendActionState.Done

    BackHandler(enabled = !isUploading && !isDone) {
        state.eventSink(MediaPreviewEvents.Cancel)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(
                        onClick = { state.eventSink(MediaPreviewEvents.Cancel) },
                    )
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                localMediaRenderer.Render(state.localMedia)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                if (state.showOptimizationOptions) {
                    OptimizationOptions(
                        isImageFile = state.localMedia.info.mimeType.isMimeTypeImage(),
                        isVideoFile = state.localMedia.info.mimeType.isMimeTypeVideo(),
                        isImageOptimizationEnabled = state.isImageOptimizationEnabled,
                        selectedVideoPreset = state.selectedVideoPreset,
                        videoSizeEstimations = state.videoSizeEstimations,
                        onToggleImageOptimization = { enabled ->
                            state.eventSink(MediaPreviewEvents.ToggleImageOptimization(enabled))
                        },
                        onShowVideoQualityDialog = {
                            state.eventSink(MediaPreviewEvents.ShowVideoQualityDialog)
                        },
                    )
                }

                MediaPreviewBottomActions(
                    state = state,
                    onSendClick = { state.eventSink(MediaPreviewEvents.Send) },
                )
            }
        }
    }

    SendActionStateDialog(
        sendActionState = state.sendActionState,
        onRetry = { state.eventSink(MediaPreviewEvents.Retry) },
        onDismiss = { state.eventSink(MediaPreviewEvents.ClearError) },
    )

    if (state.showVideoQualityDialog) {
        VideoQualityDialog(
            selectedPreset = state.selectedVideoPreset ?: VideoCompressionPreset.STANDARD,
            videoSizeEstimations = state.videoSizeEstimations,
            maxFileUploadSize = state.maxUploadSize,
            onSubmit = { preset ->
                state.eventSink(MediaPreviewEvents.SelectVideoQuality(preset))
            },
            onDismiss = {
                state.eventSink(MediaPreviewEvents.DismissVideoQualityDialog)
            },
        )
    }
}

@Composable
private fun SendActionStateDialog(
    sendActionState: SendActionState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (sendActionState) {
        is SendActionState.Sending.Processing -> {
            if (sendActionState.displayProgress) {
                ProgressDialog(
                    type = ProgressDialogType.Indeterminate,
                    text = stringResource(CommonStrings.common_preparing),
                    showCancelButton = true,
                    onDismissRequest = onDismiss,
                )
            }
        }
        is SendActionState.Sending.Uploading -> {
            ProgressDialog(
                type = ProgressDialogType.Indeterminate,
                text = stringResource(CommonStrings.common_sending),
                showCancelButton = true,
                onDismissRequest = onDismiss,
            )
        }
        is SendActionState.Failure -> {
            RetryDialog(
                content = sendActionState.error.message ?: stringResource(CommonStrings.common_error),
                onDismiss = onDismiss,
                onRetry = onRetry,
            )
        }
        else -> Unit
    }
}

@Composable
private fun OptimizationOptions(
    isImageFile: Boolean,
    isVideoFile: Boolean,
    isImageOptimizationEnabled: Boolean,
    selectedVideoPreset: VideoCompressionPreset?,
    videoSizeEstimations: List<VideoUploadEstimation>,
    onToggleImageOptimization: (Boolean) -> Unit,
    onShowVideoQualityDialog: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ElementTheme.colors.bgCanvasDefault)
    ) {
        if (isImageFile) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .niceClickable {
                        onToggleImageOptimization(!isImageOptimizationEnabled)
                    }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                    text = stringResource(MediaPreviewR.string.screen_media_upload_preview_image_optimization),
                    style = ElementTheme.typography.fontBodyLgMedium,
                )
                Switch(
                    modifier = Modifier.height(32.dp),
                    checked = isImageOptimizationEnabled,
                    onCheckedChange = onToggleImageOptimization,
                )
            }
        }

        if (isVideoFile) {
            val sizeFormatter = rememberFileSizeFormatter()
            val estimation = selectedVideoPreset?.let { preset ->
                videoSizeEstimations.find { it.preset == preset }
            }
            val estimationMb = estimation?.sizeInBytes?.let {
                if (it > 0) sizeFormatter.format(it, true) else null
            }
            val qualityTitle = buildString {
                append(selectedVideoPreset?.title() ?: stringResource(CommonStrings.common_video_quality_standard))
                if (estimationMb != null) {
                    append(" ($estimationMb)")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .niceClickable { onShowVideoQualityDialog() }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = qualityTitle,
                    style = ElementTheme.typography.fontBodyLgMedium,
                )
                Text(
                    text = stringResource(MediaPreviewR.string.screen_media_upload_preview_change_video_quality_prompt),
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun VideoQualityDialog(
    selectedPreset: VideoCompressionPreset,
    videoSizeEstimations: List<VideoUploadEstimation>,
    maxFileUploadSize: Long,
    onSubmit: (VideoCompressionPreset) -> Unit,
    onDismiss: () -> Unit,
) {
    val sizeFormatter = rememberFileSizeFormatter()
    var localSelectedPreset by remember(selectedPreset) { mutableStateOf(selectedPreset) }

    val subtitlePartNoFileSize = stringResource(CommonStrings.dialog_video_quality_selector_subtitle_no_file_size)
    val subtitlePartWithFileSize = stringResource(CommonStrings.dialog_video_quality_selector_subtitle_file_size)
    val subtitle = remember(maxFileUploadSize) {
        buildString {
            append(subtitlePartNoFileSize)
            append(String.format(subtitlePartWithFileSize, sizeFormatter.format(maxFileUploadSize, true)))
        }
    }

    ListDialog(
        title = stringResource(CommonStrings.dialog_video_quality_selector_title),
        subtitle = subtitle,
        onSubmit = { onSubmit(localSelectedPreset) },
        onDismissRequest = onDismiss,
        applyPaddingToContents = false,
    ) {
        for (estimation in videoSizeEstimations) {
            val preset = estimation.preset
            val isSelected = preset == localSelectedPreset
            item(
                key = preset,
                contentType = preset,
            ) {
                val estimationMb = sizeFormatter.format(estimation.sizeInBytes, true)
                val title = "${preset.title()} ($estimationMb)"
                ListItem(
                    headlineContent = {
                        Text(
                            text = title,
                            style = ElementTheme.typography.fontBodyLgMedium,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = preset.subtitle(),
                            style = ElementTheme.typography.fontBodyMdRegular,
                            color = ElementTheme.colors.textSecondary,
                        )
                    },
                    leadingContent = ListItemContent.RadioButton(selected = isSelected),
                    onClick = { localSelectedPreset = preset },
                    enabled = estimation.canUpload,
                )
            }
        }
    }
}

@Composable
private fun MediaPreviewBottomActions(
    state: MediaPreviewState,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextComposer(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .imePadding(),
        state = state.textEditorState,
        voiceMessageState = VoiceMessageState.Idle,
        composerMode = MessageComposerMode.Attachment,
        onRequestFocus = {},
        onSendMessage = onSendClick,
        showTextFormatting = false,
        onResetComposerMode = {},
        onAddAttachment = {},
        onDismissTextFormatting = {},
        onVoiceRecorderEvent = {},
        onVoicePlayerEvent = {},
        onSendVoiceMessage = {},
        onDeleteVoiceMessage = {},
        onReceiveSuggestion = {},
        resolveMentionDisplay = { _, _ -> io.element.android.wysiwyg.display.TextDisplay.Plain },
        resolveAtRoomMentionDisplay = { io.element.android.wysiwyg.display.TextDisplay.Plain },
        onError = {},
        onTyping = {},
        onSelectRichContent = {},
    )
}

@Composable
private fun VideoCompressionPreset.title(): String = stringResource(
    when (this) {
        VideoCompressionPreset.HIGH -> CommonStrings.common_video_quality_high
        VideoCompressionPreset.STANDARD -> CommonStrings.common_video_quality_standard
        VideoCompressionPreset.LOW -> CommonStrings.common_video_quality_low
    }
)

@Composable
private fun VideoCompressionPreset.subtitle(): String = stringResource(
    when (this) {
        VideoCompressionPreset.HIGH -> CommonStrings.common_video_quality_high_description
        VideoCompressionPreset.STANDARD -> CommonStrings.common_video_quality_standard_description
        VideoCompressionPreset.LOW -> CommonStrings.common_video_quality_low_description
    }
)

@PreviewsDayNight
@Composable
internal fun MediaPreviewViewPreview() = ElementPreview {
    val fakeLocalMediaRenderer = object : LocalMediaRenderer {
        @Composable
        override fun Render(localMedia: LocalMedia) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Media Preview",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    MediaPreviewView(
        state = aMediaPreviewState(),
        localMediaRenderer = fakeLocalMediaRenderer,
        modifier = Modifier
    )
}
