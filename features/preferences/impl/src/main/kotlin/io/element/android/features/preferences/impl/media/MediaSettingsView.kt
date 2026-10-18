/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.media

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.dialogs.ListDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.snackbar.LocalSnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.compose.LocalAnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction

@Composable
fun MediaSettingsView(
    state: MediaSettingsState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val analyticsService = LocalAnalyticsService.current

    val snackbarDispatcher = LocalSnackbarDispatcher.current
    val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = snackbarMessage)

    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = CommonStrings.common_media_upload_quality),
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) {
        val compressImages = state.mediaOptimizationState?.shouldCompressImages

        when (state.mediaOptimizationState) {
            null -> Unit
            is MediaOptimizationState.AllMedia -> {
                ListItem(
                    content = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_media_compression_title))
                    },
                    supportingContent = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_media_compression_description))
                    },
                    trailingContent = ListItemContent.Switch(
                        checked = compressImages ?: false,
                    ),
                    onClick = {
                        val newValue = !(compressImages ?: false)
                        analyticsService.captureInteraction(
                            if (newValue) {
                                Interaction.Name.MobileSettingsOptimizeMediaUploadsEnabled
                            } else {
                                Interaction.Name.MobileSettingsOptimizeMediaUploadsDisabled
                            }
                        )
                        state.eventSink(MediaSettingsEvent.SetCompressMedia(newValue))
                    }
                )
            }
            is MediaOptimizationState.Split -> {
                ListItem(
                    content = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_optimise_image_upload_quality_title))
                    },
                    supportingContent = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_optimise_image_upload_quality_description))
                    },
                    trailingContent = ListItemContent.Switch(
                        checked = compressImages ?: false,
                    ),
                    onClick = {
                        val newValue = !(compressImages ?: false)
                        analyticsService.captureInteraction(
                            if (newValue) {
                                Interaction.Name.MobileSettingsOptimizeMediaUploadsEnabled
                            } else {
                                Interaction.Name.MobileSettingsOptimizeMediaUploadsDisabled
                            }
                        )
                        state.eventSink(MediaSettingsEvent.SetCompressMedia(newValue))
                    }
                )

                var displaySelectorDialog by remember { mutableStateOf(false) }

                ListItem(
                    content = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_title))
                    },
                    supportingContent = {
                        val description = stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_description)
                        val quality = when (state.mediaOptimizationState.videoPreset) {
                            VideoCompressionPreset.LOW -> stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_low)
                            VideoCompressionPreset.STANDARD -> stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_standard)
                            VideoCompressionPreset.HIGH -> stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_high)
                        }
                        val descriptionWithValue = remember(quality) {
                            String.format(description, quality)
                        }
                        Text(text = descriptionWithValue)
                    },
                    onClick = { displaySelectorDialog = true },
                )

                if (displaySelectorDialog) {
                    VideoQualitySelectorDialog(
                        selectedPreset = state.mediaOptimizationState.videoPreset,
                        onSubmit = { preset ->
                            state.eventSink(MediaSettingsEvent.SetVideoUploadQuality(preset))
                            displaySelectorDialog = false
                        },
                        onDismiss = { displaySelectorDialog = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoQualitySelectorDialog(
    selectedPreset: VideoCompressionPreset,
    onSubmit: (VideoCompressionPreset) -> Unit,
    onDismiss: () -> Unit
) {
    val videoPresets = VideoCompressionPreset.entries
    var localSelectedPreset by remember { mutableStateOf(selectedPreset) }
    ListDialog(
        title = stringResource(CommonStrings.dialog_video_quality_selector_title),
        subtitle = stringResource(CommonStrings.dialog_default_video_quality_selector_subtitle),
        onSubmit = { onSubmit(localSelectedPreset) },
        onDismissRequest = onDismiss,
        applyPaddingToContents = false,
    ) {
        for (preset in videoPresets) {
            val isSelected = preset == localSelectedPreset
            item(
                key = preset,
                contentType = preset,
            ) {
                val title = when (preset) {
                    VideoCompressionPreset.LOW -> stringResource(R.string.screen_advanced_settings_optimise_video_upload_quality_low)
                    VideoCompressionPreset.STANDARD -> stringResource(R.string.screen_advanced_settings_optimise_video_upload_quality_standard)
                    VideoCompressionPreset.HIGH -> stringResource(R.string.screen_advanced_settings_optimise_video_upload_quality_high)
                }
                val subtitle = when (preset) {
                    VideoCompressionPreset.LOW -> stringResource(CommonStrings.common_video_quality_low_description)
                    VideoCompressionPreset.STANDARD -> stringResource(CommonStrings.common_video_quality_standard_description)
                    VideoCompressionPreset.HIGH -> stringResource(CommonStrings.common_video_quality_high_description)
                }
                ListItem(
                    content = {
                        Text(
                            text = title,
                            style = ElementTheme.typography.fontBodyLgMedium,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = subtitle,
                            style = ElementTheme.typography.fontBodyMdRegular,
                            color = ElementTheme.colors.textSecondary,
                        )
                    },
                    leadingContent = ListItemContent.RadioButton(
                        selected = isSelected,
                    ),
                    onClick = {
                        localSelectedPreset = preset
                    },
                )
            }
        }
    }
}

@Composable
@PreviewsDayNight
internal fun MediaSettingsViewPreview(
    @PreviewParameter(MediaSettingsStatePreviewParam::class) state: MediaSettingsState,
) {
    ElementPreview {
        MediaSettingsView(
            state = state,
            onBackClick = { },
        )
    }
}

@Composable
@PreviewsDayNight
internal fun VideoQualitySelectorDialogPreview() {
    ElementPreview {
        VideoQualitySelectorDialog(
            selectedPreset = VideoCompressionPreset.STANDARD,
            onSubmit = { /* no-op */ },
            onDismiss = { /* no-op */ }
        )
    }
}
