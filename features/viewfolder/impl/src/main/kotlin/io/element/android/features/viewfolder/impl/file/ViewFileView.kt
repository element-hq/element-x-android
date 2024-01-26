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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.async.AsyncFailure
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFileView(
    state: ViewFileState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackPressed)
                },
                title = {
                    Text(
                        text = state.name,
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            state.eventSink(ViewFileEvents.Share)
                        },
                    ) {
                        Icon(
                            resourceId = CompoundDrawables.ic_share_android,
                            contentDescription = stringResource(id = CommonStrings.action_share),
                        )
                    }
                    IconButton(
                        onClick = {
                            state.eventSink(ViewFileEvents.SaveOnDisk)
                        },
                    ) {
                        Icon(
                            resourceId = CompoundDrawables.ic_download,
                            contentDescription = stringResource(id = CommonStrings.action_save),
                        )
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            ) {
                when (state.lines) {
                    AsyncData.Uninitialized,
                    is AsyncData.Loading -> AsyncLoading()
                    is AsyncData.Success -> FileContent(
                        modifier = Modifier.weight(1f),
                        lines = state.lines.data.toImmutableList(),
                        colorationMode = state.colorationMode,
                    )
                    is AsyncData.Failure -> AsyncFailure(throwable = state.lines.error, onRetry = null)
                }
            }
        }
    )
}

@Composable
private fun FileContent(
    lines: ImmutableList<String>,
    colorationMode: ColorationMode,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
    ) {
        if (lines.isEmpty()) {
            item {
                Spacer(Modifier.size(80.dp))
                Text(
                    text = "Empty file",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            itemsIndexed(
                items = lines,
            ) { index, line ->
                LineRow(
                    lineNumber = index + 1,
                    line = line,
                    colorationMode = colorationMode,
                )
            }
        }
    }
}

@Composable
private fun LineRow(
    lineNumber: Int,
    line: String,
    colorationMode: ColorationMode,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                context.copyToClipboard(
                    line,
                    "Line copied to clipboard",
                )
            })
    ) {
        Text(
            modifier = Modifier
                .widthIn(min = 36.dp)
                .padding(horizontal = 4.dp),
            text = "$lineNumber",
            textAlign = TextAlign.End,
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodyMdMedium,
        )
        val color = ElementTheme.colors.textSecondary
        val width = 0.5.dp.value
        Text(
            modifier = Modifier
                .weight(1f)
                .drawWithContent {
                    // Using .height(IntrinsicSize.Min) on the Row does not work well inside LazyColumn
                    drawLine(
                        color = color,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = width
                    )
                    drawContent()
                }
                .padding(horizontal = 4.dp),
            text = line,
            color = line.toColor(colorationMode),
            style = ElementTheme.typography.fontBodyMdRegular
        )
    }
}

/**
 * Convert a line to a color.
 * Ex for logcat:
 * `01-23 13:14:50.740 25818 25818 D org.matrix.rust.sdk: elementx: SyncIndicator = Hide | RustRoomListService.kt:81`
 *                                 ^ use this char to determine the color
 * Ex for Rust logs:
 * `2024-01-26T10:22:26.947416Z  WARN elementx: Restore with non-empty map | MatrixClientsHolder.kt:68`
 *                                  ^ use this char to determine the color, see [LogLevel]
 */
@Composable
private fun String.toColor(colorationMode: ColorationMode): Color {
    return when (colorationMode) {
        ColorationMode.Logcat -> when (getOrNull(31)) {
            'D' -> colorDebug
            'I' -> colorInfo
            'W' -> colorWarning
            'E' -> colorError
            'A' -> colorError
            else -> ElementTheme.colors.textPrimary
        }
        ColorationMode.RustLogs -> when (getOrNull(32)) {
            'E' -> ElementTheme.colors.textPrimary
            'G' -> colorDebug
            'O' -> colorInfo
            'N' -> colorWarning
            'R' -> colorError
            else -> ElementTheme.colors.textPrimary
        }
        ColorationMode.None -> ElementTheme.colors.textPrimary
    }
}

private val colorDebug = Color(0xFF299999)
private val colorInfo = Color(0xFFABC023)
private val colorWarning = Color(0xFFBBB529)
private val colorError = Color(0xFFFF6B68)

@PreviewsDayNight
@Composable
internal fun ViewFileViewPreview(@PreviewParameter(ViewFileStateProvider::class) state: ViewFileState) = ElementPreview {
    ViewFileView(
        state = state,
        onBackPressed = {},
    )
}
