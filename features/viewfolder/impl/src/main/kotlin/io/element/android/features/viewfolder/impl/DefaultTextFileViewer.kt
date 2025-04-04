/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.viewfolder.api.TextFileViewer
import io.element.android.features.viewfolder.impl.file.ColorationMode
import io.element.android.features.viewfolder.impl.file.FileContent
import io.element.android.libraries.di.AppScope
import kotlinx.collections.immutable.ImmutableList
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultTextFileViewer @Inject constructor() : TextFileViewer {
    @Composable
    override fun Render(
        lines: ImmutableList<String>,
        modifier: Modifier
    ) {
        FileContent(
            lines = lines,
            colorationMode = ColorationMode.None,
            modifier = modifier
        )
    }
}
