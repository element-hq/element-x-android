/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.file

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun FileContent(
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
                    text = stringResource(CommonStrings.common_empty_file),
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
                    text = line,
                    toastMessage = context.getString(CommonStrings.common_line_copied_to_clipboard),
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
