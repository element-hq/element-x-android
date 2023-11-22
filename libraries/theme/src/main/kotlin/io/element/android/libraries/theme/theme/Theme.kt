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

package io.element.android.libraries.theme.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class Theme {
    System,
    Dark,
    Light;
}

val themes = listOf(Theme.System, Theme.Dark, Theme.Light)

@Composable
fun Theme.isDark(): Boolean {
    return when (this) {
        Theme.System -> isSystemInDarkTheme()
        Theme.Dark -> true
        Theme.Light -> false
    }
}

fun Flow<String?>.mapToTheme(): Flow<Theme> = map {
    when (it) {
        null -> Theme.System
        else -> Theme.valueOf(it)
    }
}
