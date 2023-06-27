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

package io.element.android.libraries.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.theme.previews.ColorsSchemePreview
import io.element.android.libraries.theme.materialColorSchemeDark
import io.element.android.libraries.theme.materialColorSchemeLight

@Preview
@Composable
fun ColorsSchemePreviewLight() = ColorsSchemePreview(
    Color.Black,
    Color.White,
    materialColorSchemeLight,
)

@Preview
@Composable
fun ColorsSchemePreviewDark() = ColorsSchemePreview(
    Color.White,
    Color.Black,
    materialColorSchemeDark,
)
