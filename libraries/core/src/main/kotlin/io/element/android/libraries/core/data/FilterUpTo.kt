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

package io.element.android.libraries.core.data

/**
 * Returns a list containing first [count] elements matching the given [predicate].
 * If the list contains less elements matching the [predicate], then all of them are returned.
 *
 * @param T the type of elements contained in the list.
 * @param count the maximum number of elements to take.
 * @param predicate the predicate used to match elements.
 * @return a list containing first [count] elements matching the given [predicate].
 */
inline fun <T> Iterable<T>.filterUpTo(count: Int, predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (element in this) {
        if (predicate(element)) {
            result.add(element)
            if (result.size == count) {
                break
            }
        }
    }
    return result
}
