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

package io.element.android.libraries.matrix.api.core

import io.element.android.libraries.androidutils.metadata.isInDebug
import java.io.Serializable

@JvmInline
value class ThreadId(val value: String) : Serializable {
    init {
        if (isInDebug && !MatrixPatterns.isThreadId(value)) {
            error(
                "`$value` is not a valid thread id.\n" +
                    "Thread ids are the same as event ids.\n" +
                    "Example thread id: `\$Rqnc-F-dvnEYJTyHq_iKxU2bZ1CI92-kuZq3a5lr5Zg`."
            )
        }
    }

    override fun toString(): String = value
}

fun ThreadId.asEventId(): EventId = EventId(value)
