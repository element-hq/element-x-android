/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl.migration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.features.roomlist.impl.R
import io.element.android.libraries.designsystem.atomic.pages.SunsetPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@Composable
fun MigrationScreenView(
    isMigrating: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isMigrating,
        enter = fadeIn(),
        exit = fadeOut(),
        label = "Migration view fade",
    ) {
        SunsetPage(
            modifier = modifier,
            isLoading = true,
            title = stringResource(id = R.string.screen_migration_title),
            subtitle = stringResource(id = R.string.screen_migration_message),
            overallContent = {}
        )
    }
}

@Composable
@PreviewsDayNight
internal fun MigrationViewPreview() = ElementPreview {
    MigrationScreenView(isMigrating = true)
}
