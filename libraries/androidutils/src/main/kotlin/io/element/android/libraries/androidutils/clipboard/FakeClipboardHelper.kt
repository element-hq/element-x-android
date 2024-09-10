/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.androidutils.clipboard

class FakeClipboardHelper : ClipboardHelper {
    var clipboardContents: Any? = null

    override fun copyPlainText(text: String) {
        clipboardContents = text
    }
}
