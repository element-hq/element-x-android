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

package io.element.android.libraries.core.data

/**
 * Wrapper for a CharSequence, which support mutation of the CharSequence.
 */
class StableCharSequence(val charSequence: CharSequence) {
    private val hash = charSequence.toString().hashCode()

    override fun hashCode() = hash
    override fun equals(other: Any?) = other is StableCharSequence && other.hash == hash

    override fun toString(): String = "StableCharSequence(\"$charSequence\")"
}

fun CharSequence.toStableCharSequence() = StableCharSequence(this)
