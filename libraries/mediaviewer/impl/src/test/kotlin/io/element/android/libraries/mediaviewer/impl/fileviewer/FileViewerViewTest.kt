/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.mediaviewer.impl.fileviewer

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class FileViewerViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<FileViewerEvent>(expectEvents = false)
        ensureCalledOnce {
            setFileViewerView(
                state = aFileViewerState(eventSink = eventsRecorder),
                onBackClick = it,
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on share emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<FileViewerEvent>()
        setFileViewerView(
            state = aFileViewerState(
                localMedia = AsyncData.Success(LocalMedia(Uri.EMPTY, aJsonMediaInfo())),
                eventSink = eventsRecorder,
            ),
        )
        clickOnContentDescription(CommonStrings.action_share)
        eventsRecorder.assertSingle(FileViewerEvent.Share)
    }

    @Test
    fun `clicking on download emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<FileViewerEvent>()
        setFileViewerView(
            state = aFileViewerState(
                localMedia = AsyncData.Success(LocalMedia(Uri.EMPTY, aJsonMediaInfo())),
                eventSink = eventsRecorder,
            ),
        )
        clickOnContentDescription(CommonStrings.action_download)
        eventsRecorder.assertSingle(FileViewerEvent.SaveOnDisk)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.clickOnContentDescription(@StringRes res: Int) {
    val text = activity!!.getString(res)
    onNode(hasContentDescription(text) and hasClickAction()).performClick()
}

private fun AndroidComposeUiTest<ComponentActivity>.setFileViewerView(
    state: FileViewerState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        FileViewerView(
            state = state,
            textFileViewer = { _, _ -> },
            onBackClick = onBackClick,
        )
    }
}
