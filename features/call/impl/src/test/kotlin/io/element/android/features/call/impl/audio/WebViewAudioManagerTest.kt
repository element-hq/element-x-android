/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.audio

import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewAudioManagerTest {
    @Test
    fun `start and stop call`() = runTest {
        val webView = WebView(InstrumentationRegistry.getInstrumentation().context)
        val sut = WebViewAudioManager(
            coroutineScope = backgroundScope,
            webView = webView,
        )
        sut.onCallStarted()
        delay(1000)
        sut.onCallStopped()
    }
}
