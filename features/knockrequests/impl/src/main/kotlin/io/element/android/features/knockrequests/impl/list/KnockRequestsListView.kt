/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun KnockRequestsListView(
    state: KnockRequestsListState,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "KnockRequestsList feature view",
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun KnockRequestsListViewPreview(
    @PreviewParameter(KnockRequestsListStateProvider::class) state: KnockRequestsListState
) = ElementPreview {
    KnockRequestsListView(
        state = state,
    )
}
