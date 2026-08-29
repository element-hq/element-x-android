/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.libraries.mediaviewer.impl.fileviewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.viewfolder.api.TextFileViewer
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.async.AsyncFailure
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaView
import io.element.android.libraries.mediaviewer.impl.util.bgCanvasWithTransparency
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun FileViewerView(
    state: FileViewerState,
    textFileViewer: TextFileViewer,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)
    Scaffold(
        modifier = modifier,
        topBar = {
            FileViewerTopBar(
                filename = state.filename,
                actionsEnabled = state.localMedia.isSuccess(),
                onBackClick = onBackClick,
                onShareClick = { state.eventSink(FileViewerEvent.Share) },
                onSaveClick = { state.eventSink(FileViewerEvent.SaveOnDisk) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (val localMedia = state.localMedia) {
            AsyncData.Uninitialized,
            is AsyncData.Loading -> AsyncLoading(
                modifier = Modifier.padding(padding),
            )
            is AsyncData.Failure -> AsyncFailure(
                modifier = Modifier.padding(padding),
                throwable = localMedia.error,
                onRetry = null,
            )
            is AsyncData.Success -> {
                // Do not apply the padding, the content is rendered behind the translucent top bar,
                // the same way the media viewer does.
                LocalMediaView(
                    localMedia = localMedia.data,
                    bottomPaddingInPixels = 0,
                    audioFocus = null,
                    onClick = {},
                    onOpenWith = null,
                    textFileViewer = textFileViewer,
                    forPreview = false,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun FileViewerTopBar(
    filename: String,
    actionsEnabled: Boolean,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    TopAppBar(
        titleStr = filename,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = bgCanvasWithTransparency,
        ),
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            IconButton(
                onClick = onShareClick,
                enabled = actionsEnabled,
            ) {
                Icon(
                    imageVector = CompoundIcons.ShareAndroid(),
                    contentDescription = stringResource(id = CommonStrings.action_share),
                )
            }
            IconButton(
                onClick = onSaveClick,
                enabled = actionsEnabled,
            ) {
                Icon(
                    imageVector = CompoundIcons.Download(),
                    contentDescription = stringResource(id = CommonStrings.action_download),
                )
            }
        },
    )
}

@PreviewsDayNight
@Composable
internal fun FileViewerViewPreview(
    @PreviewParameter(FileViewerStatePreviewParam::class) state: FileViewerState,
) = ElementPreview {
    FileViewerView(
        state = state,
        textFileViewer = { _, _ -> },
        onBackClick = {},
    )
}
