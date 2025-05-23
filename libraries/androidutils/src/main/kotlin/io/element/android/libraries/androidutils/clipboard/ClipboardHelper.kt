/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.clipboard

/**
 * Wrapper class for handling clipboard operations so it can be used in JVM environments.
 */
interface ClipboardHelper {
    fun copyPlainText(text: String)
}
