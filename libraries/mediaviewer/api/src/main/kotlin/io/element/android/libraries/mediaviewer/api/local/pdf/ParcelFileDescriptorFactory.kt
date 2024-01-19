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

package io.element.android.libraries.mediaviewer.api.local.pdf

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

class ParcelFileDescriptorFactory(private val context: Context) {
    fun create(model: Any?) = runCatching {
        when (model) {
            is File -> ParcelFileDescriptor.open(model, ParcelFileDescriptor.MODE_READ_ONLY)
            is Uri -> context.contentResolver.openFileDescriptor(model, "r")!!
            else -> error(RuntimeException("Can't handle this model"))
        }
    }
}
