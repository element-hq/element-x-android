/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.time

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.time.Duration.Companion.seconds

@RunWith(value = Parameterized::class)
class DurationFormatTest(
    private val seconds: Double,
    private val output: String,
) {
    companion object {
        @Parameterized.Parameters(name = "{index}: format({0})={1}")
        @JvmStatic
        fun data(): Iterable<Array<Any>> {
            return arrayListOf(
                arrayOf<Any>(0, "0:00"),
                arrayOf<Any>(1, "0:01"),
                arrayOf<Any>(10, "0:10"),
                arrayOf<Any>(59.9, "0:59"),
                arrayOf<Any>(60, "1:00"),
                arrayOf<Any>(61, "1:01"),
                arrayOf<Any>(60 * 60, "60:00"),
                arrayOf<Any>(-60, "-1:00"),
                arrayOf<Any>(-1, "-0:01"),
            ).toList()
        }
    }

    @Test
    fun formatShort() {
        assertEquals(output, seconds.seconds.formatShort())
    }
}
