/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
