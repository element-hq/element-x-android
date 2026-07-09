/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.folder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.Inject
import io.element.android.features.viewfolder.impl.model.Item

@Inject
class ViewFolderNode(
    private val presenterFactory: ViewFolderPresenter.Factory,
) {
    @Composable
    fun View(
        canGoUp: Boolean,
        path: String,
        onBackClick: () -> Unit,
        onNavigateToItem: (Item) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val presenter = remember(canGoUp, path) {
            presenterFactory.create(
                canGoUp = canGoUp,
                path = path,
            )
        }
        val state = presenter.present()
        ViewFolderView(
            state = state,
            modifier = modifier,
            onNavigateTo = onNavigateToItem,
            onBackClick = onBackClick,
        )
    }
}
