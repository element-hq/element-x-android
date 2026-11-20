/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.androidutils.clipboard.ClipboardHelper
import io.element.android.libraries.androidutils.toast.ToastHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class ViewFilePresenter(
    @Assisted val path: String,
    @Assisted val name: String,
    private val fileContentReader: FileContentReader,
    private val fileShare: FileShare,
    private val fileSave: FileSave,
    private val clipboardHelper: ClipboardHelper,
    private val toastHelper: ToastHelper,
) : Presenter<ViewFileState> {
    @AssistedFactory
    interface Factory {
        fun create(
            path: String,
            name: String,
        ): ViewFilePresenter
    }

    @Composable
    override fun present(): ViewFileState {
        val coroutineScope = rememberCoroutineScope()
        val colorationMode = remember { name.toColorationMode() }

        fun handleEvent(event: ViewFileEvent) {
            when (event) {
                ViewFileEvent.Share -> coroutineScope.share(path)
                ViewFileEvent.SaveOnDisk -> coroutineScope.save(path)
                is ViewFileEvent.CopyToClipboard -> clipboardHelper.copyPlainText(event.text) {
                    toastHelper.show(CommonStrings.common_line_copied_to_clipboard)
                }
            }
        }

        var lines: AsyncData<List<String>> by remember { mutableStateOf(AsyncData.Loading()) }
        LaunchedEffect(Unit) {
            lines = fileContentReader.getLines(path).fold(
                onSuccess = { AsyncData.Success(it) },
                onFailure = { AsyncData.Failure(it) }
            )
        }
        return ViewFileState(
            name = name,
            lines = lines,
            colorationMode = colorationMode,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.share(path: String) = launch {
        fileShare.share(path)
    }

    private fun CoroutineScope.save(path: String) = launch {
        fileSave.save(path)
    }
}

private fun String.toColorationMode(): ColorationMode {
    return when {
        equals("logcat.log") -> ColorationMode.Logcat
        startsWith("logs.") -> ColorationMode.RustLogs
        else -> ColorationMode.None
    }
}
