/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.theme.components.Button

@Composable
fun BoxScope.ProtectedView(
    hideContent: Boolean,
    onShowClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (hideContent) {
        // TODO Update design, wording for video?
        Button(
            modifier = modifier.align(Alignment.Center),
            text = "Show",
            onClick = onShowClick,
        )
    } else {
        content()
    }
}
