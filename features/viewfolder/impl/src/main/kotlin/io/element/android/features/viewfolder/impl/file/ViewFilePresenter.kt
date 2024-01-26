/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.viewfolder.impl.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ViewFilePresenter @AssistedInject constructor(
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
