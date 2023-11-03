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
