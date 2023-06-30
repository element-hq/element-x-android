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

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle

// Temporary style for text that needs to be aligned without weird font padding issues.  `includeFontPadding` will default to false in a future version of
// compose, at which point this can be removed.
//
// Ref: https://medium.com/androiddevelopers/fixing-font-padding-in-compose-text-768cd232425b
@Suppress("DEPRECATION")
val noFontPadding: TextStyle = TextStyle(
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    )
)
