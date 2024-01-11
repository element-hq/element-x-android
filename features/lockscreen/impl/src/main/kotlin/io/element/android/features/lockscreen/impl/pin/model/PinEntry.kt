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

package io.element.android.features.lockscreen.impl.pin.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

data class PinEntry(
    val digits: ImmutableList<PinDigit>,
) {
    companion object {
        fun createEmpty(size: Int): PinEntry {
            val digits = List(size) { PinDigit.Empty }
            return PinEntry(
                digits = digits.toPersistentList()
            )
        }
    }

    val size = digits.size

    /**
     * Fill the first digits with the given text.
     * Can't be more than the size of the PinEntry
     * Keep the Empty digits at the end
     * @return the new PinEntry
     */
    fun fillWith(text: String): PinEntry {
        val newDigits = MutableList<PinDigit>(size) { PinDigit.Empty }
        text.forEachIndexed { index, char ->
            if (index < size && char.isDigit()) {
                newDigits[index] = PinDigit.Filled(char)
            }
        }
        return copy(digits = newDigits.toPersistentList())
    }

    fun deleteLast(): PinEntry {
        if (isEmpty()) return this
        val newDigits = digits.toMutableList()
        newDigits.indexOfLast { it is PinDigit.Filled }.also { lastFilled ->
            newDigits[lastFilled] = PinDigit.Empty
        }
        return copy(digits = newDigits.toPersistentList())
    }

    fun addDigit(digit: Char): PinEntry {
        if (isComplete()) return this
        val newDigits = digits.toMutableList()
        newDigits.indexOfFirst { it is PinDigit.Empty }.also { firstEmpty ->
            newDigits[firstEmpty] = PinDigit.Filled(digit)
        }
        return copy(digits = newDigits.toPersistentList())
    }

    fun clear(): PinEntry {
        return createEmpty(size)
    }

    fun isComplete(): Boolean {
        return digits.all { it is PinDigit.Filled }
    }

    fun isEmpty(): Boolean {
        return digits.all { it is PinDigit.Empty }
    }

    fun toText(): String {
        return digits.joinToString("") {
            it.toText()
        }
    }
}
