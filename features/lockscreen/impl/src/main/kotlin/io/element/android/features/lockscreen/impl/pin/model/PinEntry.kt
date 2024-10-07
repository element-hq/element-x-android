/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
