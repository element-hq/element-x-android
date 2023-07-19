/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.appnav.loggedin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SyncStateView(
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    val animationSpec = spring<Float>(stiffness = 500F)
    AnimatedVisibility(
        modifier = modifier,
        visible = syncState.mustBeVisible(),
        enter = fadeIn(animationSpec = animationSpec),
        exit = fadeOut(animationSpec = animationSpec),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .background(color = ElementTheme.colors.bgSubtleSecondary)
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .progressSemantics()
                        .size(12.dp),
                    color = ElementTheme.colors.textPrimary,
                    strokeWidth = 1.5.dp,
                )
                Text(
                    text = stringResource(id = CommonStrings.common_syncing),
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontBodyMdMedium
                )
            }
        }
    }
}

private fun SyncState.mustBeVisible() = when (this) {
    SyncState.Idle -> true
    SyncState.Syncing -> false
    SyncState.InError -> false /* In this case, the network error banner can be displayed */
    SyncState.Terminated -> false
}

@DayNightPreviews
@Composable
fun SyncStateViewPreview() = ElementPreview {
    // Add a box to see the shadow
    Box(modifier = Modifier.padding(24.dp)) {
        SyncStateView(
            syncState = SyncState.Idle
        )
    }
}
