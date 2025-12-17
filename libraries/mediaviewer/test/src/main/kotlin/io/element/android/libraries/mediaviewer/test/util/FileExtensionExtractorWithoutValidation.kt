/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.test.util

import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor

class FileExtensionExtractorWithoutValidation : FileExtensionExtractor {
    override fun extractFromName(name: String): String {
        return name.substringAfterLast('.', "")
    }
}
