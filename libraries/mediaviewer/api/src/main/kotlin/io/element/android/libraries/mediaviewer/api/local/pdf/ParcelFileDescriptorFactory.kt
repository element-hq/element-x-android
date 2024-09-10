/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
