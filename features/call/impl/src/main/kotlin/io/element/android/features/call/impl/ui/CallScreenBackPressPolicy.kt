/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui
internal sealed interface CallScreenBackPressAction {
    data object DispatchEscapeToWebView : CallScreenBackPressAction
    data object EnterPictureInPicture : CallScreenBackPressAction
}

internal object CallScreenBackPressPolicy {
    fun resolve(
        supportPip: Boolean,
        hasWebView: Boolean,
        fromNative: Boolean,
    ): CallScreenBackPressAction? {
        return when {
            hasWebView && fromNative -> CallScreenBackPressAction.DispatchEscapeToWebView
            hasWebView && supportPip -> CallScreenBackPressAction.EnterPictureInPicture
            else -> null
        }
    }
}
