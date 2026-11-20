/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.mediaviewer.impl.local.audio

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.bumble.appyx.core.node.LocalNodeTargetVisibility
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.media.WaveFormSamples
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.anAudioMediaInfo
import io.element.android.libraries.mediaviewer.impl.local.rememberLocalMediaViewState
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.tests.testutils.setSafeContent
import org.junit.Test

class MediaAudioViewTest : RobolectricTest() {
    @Test
    fun `an audio file without a waveform shows the filename`() = runAndroidComposeUiTest {
        val info = anAudioMediaInfo(filename = "track.mp3")
        setMediaAudioView(info = info)
        onNodeWithText("track.mp3").assertIsDisplayed()
    }

    @Test
    fun `an audio file with a waveform hides the filename in favour of the waveform`() = runAndroidComposeUiTest {
        val info = anAudioMediaInfo(
            filename = "voice.ogg",
            waveForm = WaveFormSamples.realisticWaveForm,
        )
        setMediaAudioView(info = info)
        onNodeWithText("voice.ogg").assertDoesNotExist()
    }

    @Test
    fun `audio is paused when the view is not displayed`() = runAndroidComposeUiTest {
        setMediaAudioView(isDisplayed = false)
        waitForIdle()
    }

    @Test
    fun `audio is paused when the host node is not visible`() = runAndroidComposeUiTest {
        setMediaAudioView(isTargetVisible = false)
        waitForIdle()
    }

    @Test
    fun `a local media uri is prepared on the player`() = runAndroidComposeUiTest {
        setMediaAudioView(
            info = anAudioMediaInfo(
                filename = "voice.ogg",
                waveForm = WaveFormSamples.realisticWaveForm,
            ),
            localMediaUri = Uri.parse("file://voice.ogg"),
        )
        waitForIdle()
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setMediaAudioView(
    info: MediaInfo = anAudioMediaInfo(),
    isDisplayed: Boolean = true,
    isTargetVisible: Boolean = true,
    localMediaUri: Uri? = null,
) {
    setSafeContent {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
            LocalNodeTargetVisibility provides isTargetVisible,
        ) {
            ElementTheme {
                MediaAudioView(
                    localMediaViewState = rememberLocalMediaViewState(),
                    bottomPaddingInPixels = 0,
                    localMedia = localMediaUri?.let { aLocalMedia(uri = it, mediaInfo = info) },
                    info = info,
                    audioFocus = null,
                    isDisplayed = isDisplayed,
                )
            }
        }
    }
}
