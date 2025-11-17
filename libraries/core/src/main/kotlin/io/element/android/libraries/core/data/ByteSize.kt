/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.data

private const val kilo = 1024L
private const val mega = kilo * kilo
private const val giga = mega * kilo

enum class ByteUnit(val multiplier: Long) {
    BYTES(1L),
    KB(kilo),
    MB(mega),
    GB(giga)
}

class ByteSize internal constructor(val value: Long, val unit: ByteUnit) {
    fun to(dest: ByteUnit): Long {
        if (unit == dest) return value
        return (value * unit.multiplier) / dest.multiplier
    }
}

val Number.gigaBytes get()= ByteSize(toLong(), ByteUnit.GB)
val Number.megaBytes get()= ByteSize(toLong(), ByteUnit.MB)
val Number.kiloBytes get()= ByteSize(toLong(), ByteUnit.KB)
val Number.bytes get()= ByteSize(toLong(), ByteUnit.BYTES)
