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
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class ViewFilePresenter(
    @Assisted("path") val path: String,
    @Assisted("name") val name: String,
    private val fileContentReader: FileContentReader,
    private val fileShare: FileShare,
    private val fileSave: FileSave,
) : Presenter<ViewFileState> {
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("path") path: String,
            @Assisted("name") name: String,
        ): ViewFilePresenter
    }

    @Composable
    override fun present(): ViewFileState {
        val coroutineScope = rememberCoroutineScope()
        val colorationMode = remember { name.toColorationMode() }

        fun handleEvent(event: ViewFileEvents) {
            when (event) {
                ViewFileEvents.Share -> coroutineScope.share(path)
                ViewFileEvents.SaveOnDisk -> coroutineScope.save(path)
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
