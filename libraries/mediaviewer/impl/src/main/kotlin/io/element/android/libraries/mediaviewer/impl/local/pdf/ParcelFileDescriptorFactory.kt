/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.pdf

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import io.element.android.libraries.core.extensions.runCatchingExceptions
import java.io.File

class ParcelFileDescriptorFactory(private val context: Context) {
    fun create(model: Any?) = runCatchingExceptions {
        when (model) {
            is File -> ParcelFileDescriptor.open(model, ParcelFileDescriptor.MODE_READ_ONLY)
            is Uri -> context.contentResolver.openFileDescriptor(model, "r")!!
            else -> error(RuntimeException("Can't handle this model"))
        }
    }
}
