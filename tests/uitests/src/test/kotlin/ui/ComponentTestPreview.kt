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
import androidx.compose.ui.unit.Dp
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent

class ComponentTestPreview(
    private val showkaseBrowserComponent: ShowkaseBrowserComponent
) : TestPreview {
    @Composable
    override fun Content() = showkaseBrowserComponent.component()

    override val name: String = showkaseBrowserComponent.componentName

    override fun customHeightDp(): Dp? {
        return showkaseBrowserComponent.heightDp?.let { Dp(it.toFloat()) }
    }

    override fun toString(): String = showkaseBrowserComponent.componentKey
        // Strip common package beginning
        .replace("io.element.android.features.", "f.")
        .replace("io.element.android.libraries.", "l.")
        .replace("io.element.android.", "")
        // Reduce default group (if present)
        .replace("_DefaultGroup_", "_")
        // No need to include `Preview` suffix of function name
        .replace("Preview_", "_")
        // Also for preview annotated with @ElementPreview
        .replace("Preview-", "-")
}
