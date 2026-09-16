/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.number.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class Number(
    val digits: ImmutableList<Digit>,
) {
    companion object {
        fun createEmpty(size: Int): Number {
            val digits = List(size) { Digit.Empty }
            return Number(
                digits = digits.toImmutableList()
            )
        }
    }

    val size = digits.size

    /**
     * Fill the first digits with the given text.
     * Can't be more than the size of the NumberEntry
     * Keep the Empty digits at the end
     * @return the new NumberEntry
     */
    fun fillWith(text: String): Number {
        val newDigits = MutableList<Digit>(size) { Digit.Empty }
        text.forEachIndexed { index, char ->
            if (index < size && char.isDigit()) {
                newDigits[index] = Digit.Filled(char)
            }
        }
        return copy(digits = newDigits.toImmutableList())
    }

    fun length(): Int {
        return digits.count { it is Digit.Filled }
    }

    fun toText(): String {
        return digits.joinToString("") {
            it.toText()
        }
    }

    fun isComplete(): Boolean {
        return digits.all { it is Digit.Filled }
    }
}
