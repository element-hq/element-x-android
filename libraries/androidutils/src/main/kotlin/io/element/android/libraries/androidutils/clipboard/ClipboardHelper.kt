/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.clipboard

interface ClipboardHelper {
    /**
     * Copy a text to the clipboard.
     *
     * @param text the text to copy
     * @param isSensitive if true, the clip is flagged so the system clipboard preview (Android 13+) and keyboards hide its content.
     * @param onSuccessOnOldDevice invoked after the copy only on Android versions older than 13, since newer versions already
     * display a confirmation. Can be used to render a Toast or a Snackbar.
     */
    fun copyPlainText(
        text: String,
        isSensitive: Boolean = false,
        onSuccessOnOldDevice: () -> Unit,
    )
}
