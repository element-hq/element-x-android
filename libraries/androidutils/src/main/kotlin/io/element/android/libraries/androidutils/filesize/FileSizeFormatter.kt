/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.androidutils.filesize

interface FileSizeFormatter {
    /**
     * Formats a content size to be in the form of bytes, kilobytes, megabytes, etc.
     */
    fun format(fileSize: Long, useShortFormat: Boolean = true): String
}
