/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

sealed interface Audio {
    data class Data(
        val readSize: Int,
        val buffer: ShortArray,
    ) : Audio {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Data

            if (readSize != other.readSize) return false
            if (!buffer.contentEquals(other.buffer)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = readSize
            result = 31 * result + buffer.contentHashCode()
            return result
        }
    }

    data class Error(
        val audioRecordErrorCode: Int
    ) : Audio
}
