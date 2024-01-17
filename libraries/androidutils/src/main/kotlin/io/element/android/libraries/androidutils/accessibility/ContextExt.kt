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

package io.element.android.libraries.androidutils.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService

/**
 * Whether a screen reader is enabled.
 *
 * Avoid changing UI or app behavior based on the state of accessibility.
 * See [AccessibilityManager.isTouchExplorationEnabled] for more details.
 *
 * @return true if the screen reader is enabled.
 */
fun Context.isScreenReaderEnabled(): Boolean {
    val accessibilityManager = getSystemService<AccessibilityManager>()
        ?: return false

    return accessibilityManager.let {
        it.isEnabled && it.isTouchExplorationEnabled
    }
}
