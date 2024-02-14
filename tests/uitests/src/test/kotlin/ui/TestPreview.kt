/*
 * Copyright (c) 2022 New Vector Ltd
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

package ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.airbnb.android.showkase.models.ShowkaseElementsMetadata
import io.element.android.libraries.designsystem.preview.NIGHT_MODE_NAME

interface TestPreview {
    @Composable
    fun Content()

    val name: String

    fun customHeightDp(): Dp? = null
}

/**
 * Showkase doesn't put the [Preview.uiMode] parameter in its [ShowkaseElementsMetadata]
 * so we have to encode the night mode bit in a preview's name.
 */
fun TestPreview.isNightMode(): Boolean {
    // Dark mode previews have name "N" so their component name contains "- N"
    return this.name.contains("- $NIGHT_MODE_NAME")
}
