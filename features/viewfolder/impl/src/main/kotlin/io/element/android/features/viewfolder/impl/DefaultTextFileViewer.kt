/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.viewfolder.api.TextFileViewer
import io.element.android.features.viewfolder.impl.file.ColorationMode
import io.element.android.features.viewfolder.impl.file.FileContent
import io.element.android.libraries.androidutils.clipboard.ClipboardHelper
import io.element.android.libraries.androidutils.toast.ToastHelper
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@ContributesBinding(AppScope::class)
class DefaultTextFileViewer(
    private val clipboardHelper: ClipboardHelper,
    private val toastHelper: ToastHelper,
) : TextFileViewer {
    @Composable
    override fun Render(
        lines: ImmutableList<String>,
        modifier: Modifier
    ) {
        FileContent(
            lines = lines,
            colorationMode = ColorationMode.None,
            onLineClick = { line ->
                clipboardHelper.copyPlainText(line) {
                    toastHelper.show(CommonStrings.common_line_copied_to_clipboard)
                }
            },
            modifier = modifier
        )
    }
}
