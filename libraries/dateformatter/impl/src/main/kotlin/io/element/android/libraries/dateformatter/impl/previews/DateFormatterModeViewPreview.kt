/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl.previews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.dateformatter.impl.DefaultDateFormatter
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.allBooleans
import kotlin.time.Instant

@Preview
@Composable
internal fun DateFormatterModeViewPreview(
    @PreviewParameter(DateFormatterModeProvider::class) dateFormatterMode: DateFormatterMode,
) = ElementPreview {
    DateFormatterModeView(dateFormatterMode)
}

@Composable
private fun DateFormatterModeView(
    mode: DateFormatterMode,
) {
    val context = LocalContext.current
    val composeLocale = Locale.current
    val dateFormatter = remember {
        createFormatter(
            context = context,
            currentDate = dateForPreviewToday.date,
            locale = java.util.Locale.Builder()
                .setLanguageTag(composeLocale.toLanguageTag())
                .build(),
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Mode $mode / $composeLocale",
            style = ElementTheme.typography.fontHeadingSmMedium
        )
        val today = Instant.parse(dateForPreviewToday.date).toEpochMilliseconds()
        Text(
            text = "Today is: ${dateFormatter.format(today, DateFormatterMode.Full, useRelative = false)}",
            style = ElementTheme.typography.fontHeadingSmMedium,
        )
        dateForPreviews.forEach { dateForPreview ->
            DateForPreviewItem(
                dateForPreview = dateForPreview,
                dateFormatter = dateFormatter,
                mode = mode,
            )
        }
    }
}

@Composable
private fun DateForPreviewItem(
    dateForPreview: DateForPreview,
    dateFormatter: DefaultDateFormatter,
    mode: DateFormatterMode,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            text = dateForPreview.semantic,
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textSecondary,
        )
        val ts = Instant.parse(dateForPreview.date).toEpochMilliseconds()
        Row {
            Column {
                listOf("Absolute:", "Relative:").forEach { label ->
                    Text(
                        text = label,
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textPrimary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                allBooleans.forEach { useRelative ->
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = dateFormatter.format(ts, mode, useRelative),
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}
