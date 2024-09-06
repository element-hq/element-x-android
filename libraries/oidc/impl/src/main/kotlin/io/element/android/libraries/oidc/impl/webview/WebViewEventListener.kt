/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl.webview

fun interface WebViewEventListener {
    /**
     * Triggered when a Webview loads an url.
     *
     * @param url The url about to be rendered.
     * @return true if the method needs to manage some custom handling
     */
    fun shouldOverrideUrlLoading(url: String): Boolean
}
