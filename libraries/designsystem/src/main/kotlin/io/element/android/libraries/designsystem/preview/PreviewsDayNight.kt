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

package io.element.android.libraries.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.architecture.coverage.ExcludeFromJacocoGeneratedReport

/**
 * Marker for a night mode preview.
 *
 * Previews with such marker will be rendered in night mode during screenshot testing.
 *
 * NB: Length of this constant is kept to a minimum to avoid screenshot file names being too long.
 */
const val NIGHT_MODE_NAME = "Night"

/**
 * Marker for a day mode preview.
 *
 * This marker is currently not used during screenshot testing, it mainly act as a counterpart to [NIGHT_MODE_NAME].
 *
 * NB: Length of this constant is kept to a minimum to avoid screenshot file names being too long.
 */
const val DAY_MODE_NAME = "Day"

/**
 * Generates 2 previews of the composable it is applied to: day and night mode.
 *
 * NB: Content should be wrapped into [ElementPreview] to apply proper theming.
 */
@Preview(
    name = DAY_MODE_NAME,
    fontScale = 1f,
)
@Preview(
    name = NIGHT_MODE_NAME,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 1f,
)
@ExcludeFromJacocoGeneratedReport
annotation class PreviewsDayNight
