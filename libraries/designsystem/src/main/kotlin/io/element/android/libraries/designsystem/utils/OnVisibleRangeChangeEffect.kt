/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun OnVisibleRangeChangeEffect(lazyListState: LazyListState, onChange: (IntRange) -> Unit) {
    val onChangeUpdated by rememberUpdatedState(onChange)
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .map { visibleItemsInfo ->
                val firstItemIndex = visibleItemsInfo.firstOrNull()?.index ?: 0
                val size = visibleItemsInfo.size
                firstItemIndex until firstItemIndex + size
            }
            .distinctUntilChanged()
            .collectLatest { visibleRange ->
                onChangeUpdated(visibleRange)
            }
    }
}
