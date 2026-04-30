/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.ui

import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.ui.CallScreenBackPressAction
import io.element.android.features.call.impl.ui.CallScreenBackPressPolicy
import org.junit.Test

class CallScreenBackPressPolicyTest {
    @Test
    fun `resolve returns dispatch escape when a web view is available and native button is pressed`() {
        val result = CallScreenBackPressPolicy.resolve(
            supportPip = false,
            hasWebView = true,
            fromNative = true,
        )

        assertThat(result).isEqualTo(CallScreenBackPressAction.DispatchEscapeToWebView)
    }

    @Test
    fun `resolve dispatch escape when there is a web view and pip is supported on native button press`() {
        val result = CallScreenBackPressPolicy.resolve(
            supportPip = true,
            hasWebView = true,
            fromNative = true,
        )

        assertThat(result).isEqualTo(CallScreenBackPressAction.DispatchEscapeToWebView)
    }

    @Test
    fun `resolve returns hangup when there is no web view and pip is not supported from native button`() {
        val result = CallScreenBackPressPolicy.resolve(
            supportPip = false,
            hasWebView = false,
            fromNative = true,
        )

        assertThat(result).isNull()
    }

    @Test
    fun `resolve returns hangup when there is no web view even though pip is supported from native button`() {
        val result = CallScreenBackPressPolicy.resolve(
            supportPip = true,
            hasWebView = false,
            fromNative = true,
        )

        assertThat(result).isNull()
    }

    @Test
    fun `resolve goes to pip if its not from native but from the webview`() {
        val result = CallScreenBackPressPolicy.resolve(
            supportPip = true,
            hasWebView = true,
            fromNative = false,
        )

        assertThat(result).isEqualTo(CallScreenBackPressAction.EnterPictureInPicture)
    }
    @Test
    fun `resolve hangs up if its not from native but from the webview and pip is not supported`() {
        val result = CallScreenBackPressPolicy.resolve(
            supportPip = false,
            hasWebView = true,
            fromNative = false,
        )

        assertThat(result).isNull()
    }

    @Test
    fun `invalid cases (event comes from webview but there is now webview) all result in hangup`() {
        val withPipSupport = CallScreenBackPressPolicy.resolve(
            supportPip = true,
            hasWebView = false,
            fromNative = false,
        )
        assertThat(withPipSupport).isNull()
        val withOutPipSupport = CallScreenBackPressPolicy.resolve(
            supportPip = false,
            hasWebView = false,
            fromNative = false,
        )
        assertThat(withOutPipSupport).isNull()
    }
}
