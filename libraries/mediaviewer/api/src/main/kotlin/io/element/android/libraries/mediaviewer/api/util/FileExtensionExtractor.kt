/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.util

/**
 * Extracts the extension of a file name, so it can be shown to the user and used to pick an icon.
 */
interface FileExtensionExtractor {
    /**
     * @param name the file name to inspect.
     * @return the extension without its dot, or an empty string when the name has none.
     */
    fun extractFromName(name: String): String
}
