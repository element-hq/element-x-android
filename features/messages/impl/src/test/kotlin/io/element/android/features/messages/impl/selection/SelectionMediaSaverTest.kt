/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.R
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SelectionMediaSaverTest {
    @Test
    fun `saveAll reports progress for each saved file`() = runTest {
        val saver = FakeSelectionMediaSaver()
        val progress = mutableListOf<Int>()
        val saved = saver.saveAll(listOf(aSavableMedia("one.jpg"), aSavableMedia("two.jpg")), progress::add)
        assertThat(saved).isEqualTo(2)
        assertThat(progress).containsExactly(1, 2).inOrder()
        assertThat(saver.savedFilenames).containsExactly("one.jpg", "two.jpg").inOrder()
    }

    @Test
    fun `saveAll carries on after a file which cannot be saved`() = runTest {
        val saver = FakeSelectionMediaSaver(failFor = { it.filename == "one.jpg" })
        val saved = saver.saveAll(listOf(aSavableMedia("one.jpg"), aSavableMedia("two.jpg"))) {}
        assertThat(saved).isEqualTo(1)
        assertThat(saver.savedFilenames).containsExactly("two.jpg")
    }

    @Test
    fun `the message depends on how much of the batch was saved`() {
        assertThat(bulkSaveMessage(saved = 0, total = 2)).isEqualTo(CommonStrings.common_error)
        assertThat(bulkSaveMessage(saved = 1, total = 2)).isEqualTo(R.string.screen_room_selection_saved_partly)
        assertThat(bulkSaveMessage(saved = 1, total = 1)).isEqualTo(CommonStrings.common_file_saved_on_disk_android)
        assertThat(bulkSaveMessage(saved = 2, total = 2)).isEqualTo(R.string.screen_room_selection_saved)
    }
}

private fun aSavableMedia(filename: String) = SavableMedia(
    source = MediaSource(url = "mxc://server/$filename"),
    filename = filename,
    mimeType = "image/jpeg",
)
