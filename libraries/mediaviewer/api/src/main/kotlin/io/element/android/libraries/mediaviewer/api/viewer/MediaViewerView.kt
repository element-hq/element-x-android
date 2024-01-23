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

package io.element.android.libraries.mediaviewer.api.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.mediaviewer.api.R
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaView
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.rememberLocalMediaViewState
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.delay

@Composable
fun MediaViewerView(
    state: MediaViewerState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onRetry() {
        state.eventSink(MediaViewerEvents.RetryLoading)
    }

    fun onDismissError() {
        state.eventSink(MediaViewerEvents.ClearLoadingError)
    }

    val localMediaViewState = rememberLocalMediaViewState()
    val showThumbnail = !localMediaViewState.isReady
    val showProgress = rememberShowProgress(state.downloadedMedia)
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    Scaffold(
        modifier,
        topBar = {
            MediaViewerTopBar(
                actionsEnabled = state.downloadedMedia is AsyncData.Success,
                mimeType = state.mediaInfo.mimeType,
                onBackPressed = onBackPressed,
                canDownload = state.canDownload,
                canShare = state.canShare,
                eventSink = state.eventSink
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            if (showProgress) {
                LinearProgressIndicator(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                )
            } else {
                Spacer(Modifier.height(2.dp))
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (state.downloadedMedia is AsyncData.Failure) {
                    ErrorView(
                        errorMessage = stringResource(id = CommonStrings.error_unknown),
                        onRetry = ::onRetry,
                        onDismiss = ::onDismissError
                    )
                }
                LocalMediaView(
                    localMediaViewState = localMediaViewState,
                    localMedia = state.downloadedMedia.dataOrNull(),
                    mediaInfo = state.mediaInfo,
                )
                ThumbnailView(
                    mediaInfo = state.mediaInfo,
                    thumbnailSource = state.thumbnailSource,
                    showThumbnail = showThumbnail,
                )
            }
        }
    }
}

@Composable
private fun rememberShowProgress(downloadedMedia: AsyncData<LocalMedia>): Boolean {
    var showProgress by remember {
        mutableStateOf(false)
    }
    if (LocalInspectionMode.current) {
        showProgress = downloadedMedia.isLoading()
    } else {
        // Trick to avoid showing progress indicator if the media is already on disk.
        // When sdk will expose download progress we'll be able to remove this.
        LaunchedEffect(downloadedMedia) {
            showProgress = false
            delay(100)
            if (downloadedMedia.isLoading()) {
                showProgress = true
            }
        }
    }
    return showProgress
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaViewerTopBar(
    actionsEnabled: Boolean,
    canDownload: Boolean,
    canShare: Boolean,
    mimeType: String,
    onBackPressed: () -> Unit,
    eventSink: (MediaViewerEvents) -> Unit,
) {
    TopAppBar(
        title = {},
        navigationIcon = { BackButton(onClick = onBackPressed) },
        actions = {
            IconButton(
                enabled = actionsEnabled,
                onClick = {
                    eventSink(MediaViewerEvents.OpenWith)
                },
            ) {
                when (mimeType) {
                    MimeTypes.Apk -> Icon(
                        resourceId = R.drawable.ic_apk_install,
                        contentDescription = stringResource(id = CommonStrings.common_install_apk_android)
                    )
                    else -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(id = CommonStrings.action_open_with)
                    )
                }
            }
            if (canDownload) {
                IconButton(
                    enabled = actionsEnabled,
                    onClick = {
                        eventSink(MediaViewerEvents.SaveOnDisk)
                    },
                ) {
                    Icon(
                        resourceId = CompoundDrawables.ic_download,
                        contentDescription = stringResource(id = CommonStrings.action_save),
                    )
                }
            }
            if (canShare) {
                IconButton(
                    enabled = actionsEnabled,
                    onClick = {
                        eventSink(MediaViewerEvents.Share)
                    },
                ) {
                    Icon(
                        resourceId = CompoundDrawables.ic_share_android,
                        contentDescription = stringResource(id = CommonStrings.action_share)
                    )
                }
            }
        }
    )
}

@Composable
private fun ThumbnailView(
    thumbnailSource: MediaSource?,
    showThumbnail: Boolean,
    mediaInfo: MediaInfo,
) {
    AnimatedVisibility(
        visible = showThumbnail,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val mediaRequestData = MediaRequestData(
                source = thumbnailSource,
                kind = MediaRequestData.Kind.File(mediaInfo.name, mediaInfo.mimeType)
            )
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = mediaRequestData,
                alpha = 0.8f,
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RetryDialog(
        modifier = modifier,
        content = errorMessage,
        onRetry = onRetry,
        onDismiss = onDismiss
    )
}

// Only preview in dark, dark theme is forced on the Node.
@Preview
@Composable
internal fun MediaViewerViewPreview(@PreviewParameter(MediaViewerStateProvider::class) state: MediaViewerState) = ElementPreviewDark {
    MediaViewerView(
        state = state,
        onBackPressed = {}
    )
}
