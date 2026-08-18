/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.Inject

@Inject
class ViewFileNode(
    private val presenterFactory: ViewFilePresenter.Factory,
) {
    @Composable
    fun View(
        path: String,
        name: String,
        onBackClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val presenter = remember(path, name) {
            presenterFactory.create(
                path = path,
                name = name,
            )
        }
        val state = presenter.present()
        ViewFileView(
            state = state,
            modifier = modifier,
            onBackClick = onBackClick,
        )
    }
}
