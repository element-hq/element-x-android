/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.cachecleaner.api

interface CacheCleaner {
    /**
     * Clear the cache subdirs holding temporarily decrypted content (such as media and voice messages).
     *
     * Will fail silently in case of errors while deleting the files.
     */
    fun clearCache()
}
