/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.call.ui

import android.view.KeyEvent
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.call.impl.pip.PictureInPictureEvent
import io.element.android.features.call.impl.pip.aPictureInPictureState
import io.element.android.features.call.impl.ui.CallScreenEvent
import io.element.android.features.call.impl.ui.CallScreenView
import io.element.android.features.call.impl.ui.JavascriptBackHandler
import io.element.android.features.call.impl.ui.aCallScreenState
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.pressBackKey
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.Resetter
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class CallScreenViewTest {
    @Test
    fun `pressing back key triggers hangup when no web view is available and pip is unsupported`() = runAndroidComposeUiTest {
        val callEvents = EventsRecorder<CallScreenEvent>()

        setCallScreenView(
            state = aCallScreenState(eventSink = callEvents),
            useInspectionMode = true,
        )

        pressBackKey()

        callEvents.assertEmpty()
    }

    @Config(shadows = [RecordingShadowWebView::class])
    @Test
    fun `pressing back key dispatches escape key events to web view when pip is unsupported`() = runAndroidComposeUiTest {
        setCallScreenView(
            state = aCallScreenState(),
            useInspectionMode = false,
        )

        pressBackKey()

        val dispatchedEvents = RecordingShadowWebView.dispatchedEvents
        assertEquals(2, dispatchedEvents.size)
        assertEquals(KeyEvent.ACTION_DOWN, dispatchedEvents[0].action)
        assertEquals(KeyEvent.KEYCODE_ESCAPE, dispatchedEvents[0].keyCode)
        assertEquals(KeyEvent.ACTION_UP, dispatchedEvents[1].action)
        assertEquals(KeyEvent.KEYCODE_ESCAPE, dispatchedEvents[1].keyCode)
    }

    @Config(shadows = [RecordingShadowWebView::class])
    @Test
    fun `web view javascript back handler emits pip event when pip is supported`() = runAndroidComposeUiTest {
        val pipEvents = EventsRecorder<PictureInPictureEvent>()

        setCallScreenView(
            state = aCallScreenState(),
            useInspectionMode = false,
            pipState = aPictureInPictureState(
                supportPip = true,
                eventSink = pipEvents,
            ),
        )

        runOnIdle {
            RecordingShadowWebView.invokeJavascriptBackHandler()
        }

        pipEvents.assertSize(2)
        pipEvents.assertTrue(0) { it is PictureInPictureEvent.SetPipController }
        pipEvents.assertTrue(1) { it is PictureInPictureEvent.EnterPictureInPicture }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setCallScreenView(
    state: io.element.android.features.call.impl.ui.CallScreenState,
    useInspectionMode: Boolean,
    pipState: io.element.android.features.call.impl.pip.PictureInPictureState = aPictureInPictureState(supportPip = false),
) {
    setContent {
        // Inspection mode disables AndroidView creation; keep it configurable per test.
        CompositionLocalProvider(LocalInspectionMode provides useInspectionMode) {
            CallScreenView(
                state = state,
                pipState = pipState,
                onConsoleMessage = {},
                requestPermissions = { _, _ -> },
            )
        }
    }
}

@Implements(WebView::class)
internal class RecordingShadowWebView : ShadowWebView() {
    companion object {
        val dispatchedEvents = mutableListOf<KeyEvent>()
        private var backHandlerJavascriptInterface: JavascriptBackHandler? = null

        @Resetter
        @JvmStatic
        @Suppress("unused")
        fun resetRecordedEvents() {
            dispatchedEvents.clear()
            backHandlerJavascriptInterface = null
        }

        fun invokeJavascriptBackHandler() {
            val backHandler = checkNotNull(backHandlerJavascriptInterface) { "Expected backHandler JavaScript interface to be registered" }
            backHandler.onBackPressed()
        }
    }

    @Implementation
    protected override fun addJavascriptInterface(`object`: Any, name: String) {
        super.addJavascriptInterface(`object`, name)
        if (name == "backHandler") {
            backHandlerJavascriptInterface = `object` as? JavascriptBackHandler
        }
    }

    @Implementation
    @Suppress("unused")
    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        dispatchedEvents += KeyEvent(event)
        return false
    }
}
