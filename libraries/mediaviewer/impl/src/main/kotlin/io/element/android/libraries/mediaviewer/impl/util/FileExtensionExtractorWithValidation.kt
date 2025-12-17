/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.util

import android.webkit.MimeTypeMap
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor

@ContributesBinding(AppScope::class)
class FileExtensionExtractorWithValidation : FileExtensionExtractor {
    override fun extractFromName(name: String): String {
        val fileExtension = name.substringAfterLast('.', "")
        // Makes sure the extension is known by the system, otherwise default to binary extension.
        return if (MimeTypeMap.getSingleton().hasExtension(fileExtension)) {
            fileExtension
        } else {
            "bin"
        }
    }
}
