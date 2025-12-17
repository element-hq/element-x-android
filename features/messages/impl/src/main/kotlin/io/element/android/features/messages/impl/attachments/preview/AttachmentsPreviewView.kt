/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.error.sendAttachmentError
import io.element.android.features.messages.impl.attachments.video.MediaOptimizationSelectorEvent
import io.element.android.features.messages.impl.attachments.video.MediaOptimizationSelectorState
import io.element.android.features.messages.impl.attachments.video.VideoUploadEstimation
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.ProgressDialogType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.AlertDialog
import io.element.android.libraries.designsystem.components.dialogs.ListDialog
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.modifiers.niceClickable
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Switch
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaRenderer
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.textcomposer.TextComposer
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.formatter.rememberFileSizeFormatter
import io.element.android.wysiwyg.display.TextDisplay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentsPreviewView(
    state: AttachmentsPreviewState,
    localMediaRenderer: LocalMediaRenderer,
    modifier: Modifier = Modifier,
) {
    fun postSendAttachment() {
        state.eventSink(AttachmentsPreviewEvents.SendAttachment)
    }

    fun postCancel() {
        state.eventSink(AttachmentsPreviewEvents.CancelAndDismiss)
    }

    fun postClearSendState() {
        state.eventSink(AttachmentsPreviewEvents.CancelAndClearSendState)
    }

    BackHandler(enabled = state.sendActionState !is SendActionState.Sending.Uploading && state.sendActionState !is SendActionState.Done) {
        postCancel()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(
                        imageVector = CompoundIcons.Close(),
                        onClick = ::postCancel,
                    )
                },
                title = {},
            )
        }
    ) { paddingValues ->
        AttachmentPreviewContent(
            modifier = Modifier.padding(paddingValues),
            state = state,
            localMediaRenderer = localMediaRenderer,
            onSendClick = ::postSendAttachment,
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
        is SendActionState.Sending.Processing -> {
            if (sendActionState.displayProgress) {
                ProgressDialog(
                    type = ProgressDialogType.Indeterminate,
                    text = stringResource(CommonStrings.common_preparing),
                    showCancelButton = true,
                    onDismissRequest = onDismissClick,
                )
            }
        }
        is SendActionState.Sending.Uploading -> {
            ProgressDialog(
                type = ProgressDialogType.Indeterminate,
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
    state: AttachmentsPreviewState,
    localMediaRenderer: LocalMediaRenderer,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (val attachment = state.attachment) {
                is Attachment.Media -> {
                    localMediaRenderer.Render(attachment.localMedia)
                }
            }
        }
        val mimeType = (state.attachment as? Attachment.Media)?.localMedia?.info?.mimeType
        if (mimeType?.isMimeTypeImage() == true) {
            ImageOptimizationSelector(state.mediaOptimizationSelectorState)
        } else if (mimeType?.isMimeTypeVideo() == true) {
            VideoPresetSelector(state = state.mediaOptimizationSelectorState)
        }

        val sizeFormatter = rememberFileSizeFormatter()
        if (state.displayFileTooLargeError) {
            val maxFileUploadSize = state.mediaOptimizationSelectorState.maxUploadSize.dataOrNull()
            if (maxFileUploadSize != null) {
                val content = stringResource(CommonStrings.dialog_file_too_large_to_upload_subtitle, sizeFormatter.format(maxFileUploadSize, true))
                AlertDialog(
                    title = stringResource(CommonStrings.dialog_file_too_large_to_upload_title),
                    content = content,
                    onDismiss = { state.eventSink(AttachmentsPreviewEvents.CancelAndDismiss) },
                )
            }
        }

        AttachmentsPreviewBottomActions(
            state = state,
            onSendClick = onSendClick,
            modifier = Modifier
                .fillMaxWidth()
                .background(ElementTheme.colors.bgCanvasDefault)
                .height(IntrinsicSize.Min)
                .imePadding(),
        )
    }
}

@Composable
private fun ImageOptimizationSelector(state: MediaOptimizationSelectorState) {
    if (state.displayMediaSelectorViews == true) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .niceClickable {
                    state.isImageOptimizationEnabled?.let { value ->
                        state.eventSink(MediaOptimizationSelectorEvent.SelectImageOptimization(!value))
                    }
                }
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                text = stringResource(R.string.screen_media_upload_preview_optimize_image_quality_title),
                style = ElementTheme.materialTypography.bodyLarge,
            )
            Switch(
                modifier = Modifier.height(32.dp),
                checked = state.isImageOptimizationEnabled.orFalse(),
                onCheckedChange = { value -> state.eventSink(MediaOptimizationSelectorEvent.SelectImageOptimization(value)) },
            )
        }
    }
}

@Composable
private fun VideoPresetSelector(
    state: MediaOptimizationSelectorState,
) {
    val videoPresets = state.videoSizeEstimations.dataOrNull()
    var selectedPreset by remember(state.selectedVideoPreset) { mutableStateOf(state.selectedVideoPreset) }

    val displayDialog = state.displayVideoPresetSelectorDialog

    val sizeFormatter = rememberFileSizeFormatter()

    if (state.displayMediaSelectorViews == true && videoPresets != null && state.selectedVideoPreset != null) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .niceClickable { state.eventSink(MediaOptimizationSelectorEvent.OpenVideoPresetSelectorDialog) }
        ) {
            val estimation = videoPresets.find { it.preset == selectedPreset }
            val estimationMb = estimation?.sizeInBytes?.let { sizeFormatter.format(it, true) }
            val title = buildString {
                append(state.selectedVideoPreset.title())
                if (estimationMb != null) {
                    append(" ($estimationMb)")
                }
            }
            Text(text = title, style = ElementTheme.typography.fontBodyLgMedium)
            Text(
                text = stringResource(R.string.screen_media_upload_preview_change_video_quality_prompt),
                style = ElementTheme.typography.fontBodyLgMedium,
                color = ElementTheme.colors.textSecondary,
            )
        }
    }

    if (displayDialog) {
        VideoQualitySelectorDialog(
            selectedPreset = selectedPreset ?: VideoCompressionPreset.STANDARD,
            videoSizeEstimations = videoPresets ?: persistentListOf(),
            maxFileUploadSize = state.maxUploadSize.dataOrNull(),
            onSubmit = { preset ->
                selectedPreset = preset
                state.eventSink(MediaOptimizationSelectorEvent.SelectVideoPreset(preset))
            },
            onDismiss = { state.eventSink(MediaOptimizationSelectorEvent.DismissVideoPresetSelectorDialog) }
        )
    }
}

@Composable
private fun VideoQualitySelectorDialog(
    selectedPreset: VideoCompressionPreset,
    videoSizeEstimations: ImmutableList<VideoUploadEstimation>,
    maxFileUploadSize: Long?,
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
            if (maxFileUploadSize != null) {
                append(String.format(subtitlePartWithFileSize, sizeFormatter.format(maxFileUploadSize, true)))
            }
        }
    }
    ListDialog(
        title = stringResource(CommonStrings.dialog_video_quality_selector_title),
        subtitle = subtitle,
        onSubmit = { onSubmit(localSelectedPreset) },
        onDismissRequest = onDismiss,
        applyPaddingToContents = false,
    ) {
        for (videoEstimation in videoSizeEstimations) {
            val preset = videoEstimation.preset
            val isSelected = preset == localSelectedPreset
            item(
                key = preset,
                contentType = preset,
            ) {
                val estimationMb = sizeFormatter.format(videoEstimation.sizeInBytes, true)
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
                            style = ElementTheme.materialTypography.bodyMedium,
                            color = ElementTheme.colors.textSecondary,
                        )
                    },
                    leadingContent = ListItemContent.RadioButton(
                        selected = isSelected,
                    ),
                    onClick = {
                        localSelectedPreset = preset
                    },
                    enabled = videoEstimation.canUpload,
                )
            }
        }
    }
}

@Composable
private fun AttachmentsPreviewBottomActions(
    state: AttachmentsPreviewState,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextComposer(
        modifier = modifier,
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
        resolveMentionDisplay = { _, _ -> TextDisplay.Plain },
        resolveAtRoomMentionDisplay = { TextDisplay.Plain },
        onError = {},
        onTyping = {},
        onSelectRichContent = {},
    )
}

// Only preview in dark, dark theme is forced on the Node.
@Preview
@Composable
internal fun AttachmentsPreviewViewPreview(@PreviewParameter(AttachmentsPreviewStateProvider::class) state: AttachmentsPreviewState) = ElementPreviewDark {
    AttachmentsPreviewView(
        state = state,
        localMediaRenderer = object : LocalMediaRenderer {
            @Composable
            override fun Render(localMedia: LocalMedia) {
                Image(
                    painter = painterResource(id = CommonDrawables.sample_background),
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = null,
                )
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun VideoQualitySelectorDialogPreview() {
    ElementPreview {
        VideoQualitySelectorDialog(
            selectedPreset = VideoCompressionPreset.STANDARD,
            videoSizeEstimations = persistentListOf(
                VideoUploadEstimation(VideoCompressionPreset.HIGH, 2_000_000, canUpload = false),
                VideoUploadEstimation(VideoCompressionPreset.STANDARD, 1_000_000, canUpload = true),
                VideoUploadEstimation(VideoCompressionPreset.LOW, 500_000, canUpload = true)
            ),
            maxFileUploadSize = 1_500_000,
            onSubmit = {},
            onDismiss = {},
        )
    }
}

@Composable
fun VideoCompressionPreset.title(): String {
    return stringResource(
        when (this) {
            VideoCompressionPreset.STANDARD -> CommonStrings.common_video_quality_standard
            VideoCompressionPreset.HIGH -> CommonStrings.common_video_quality_high
            VideoCompressionPreset.LOW -> CommonStrings.common_video_quality_low
        }
    )
}

@Composable
fun VideoCompressionPreset.subtitle(): String {
    return stringResource(
        when (this) {
            VideoCompressionPreset.STANDARD -> CommonStrings.common_video_quality_standard_description
            VideoCompressionPreset.HIGH -> CommonStrings.common_video_quality_high_description
            VideoCompressionPreset.LOW -> CommonStrings.common_video_quality_low_description
        }
    )
}
