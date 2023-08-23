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

package io.element.android.libraries.designsystem.components.dialogs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Used to store the visual data for a list option.
 */
data class ListOption(
    val title: String,
    val subtitle: String? = null,
)

/** Creates an immutable list of [ListOption]s from the given [values], using them as titles. */
fun listOptionOf(vararg values: String): ImmutableList<ListOption> {
    return values.map { ListOption(it) }.toImmutableList()
}
