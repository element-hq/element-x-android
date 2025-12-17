/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.data

enum class ByteUnit(val bitShift: Int) {
    BYTES(0),
    KB(10),
    MB(20),
    GB(30)
}

class ByteSize internal constructor(val value: Long, val unit: ByteUnit) {
    fun to(dest: ByteUnit): Long {
        if (unit == dest) return value
        return value shl unit.bitShift shr dest.bitShift
    }
}

val Number.gigaBytes get() = ByteSize(toLong(), ByteUnit.GB)
val Number.megaBytes get() = ByteSize(toLong(), ByteUnit.MB)
val Number.kiloBytes get() = ByteSize(toLong(), ByteUnit.KB)
val Number.bytes get() = ByteSize(toLong(), ByteUnit.BYTES)
