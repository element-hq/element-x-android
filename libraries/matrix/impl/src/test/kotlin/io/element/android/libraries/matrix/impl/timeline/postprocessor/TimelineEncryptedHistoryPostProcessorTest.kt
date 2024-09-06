/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class TimelineEncryptedHistoryPostProcessorTest {
    private val defaultLastLoginTimestamp = Date(1_689_061_264L)

    @Test
    fun `given an unencrypted room, nothing is done`() = runTest {
        val processor = createPostProcessor(isRoomEncrypted = false)
        val items = listOf(
            MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem())
        )
        assertThat(processor.process(items)).isSameInstanceAs(items)
    }

    @Test
    fun `given an encrypted room, and key backup enabled, nothing is done`() = runTest {
        val processor = createPostProcessor(isKeyBackupEnabled = true)
        val items = listOf(
            MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem())
        )
        assertThat(processor.process(items)).isSameInstanceAs(items)
    }

    @Test
    fun `given a null lastLoginTimestamp, nothing is done`() = runTest {
        val processor = createPostProcessor(lastLoginTimestamp = null)
        val items = listOf(
            MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem())
        )
        assertThat(processor.process(items)).isSameInstanceAs(items)
    }

    @Test
    fun `given an empty list, nothing is done`() = runTest {
        val processor = createPostProcessor()
        val items = emptyList<MatrixTimelineItem>()
        assertThat(processor.process(items)).isSameInstanceAs(items)
    }

    @Test
    fun `given a list with no items before lastLoginTimestamp, nothing is done`() = runTest {
        val processor = createPostProcessor()
        val items = listOf(
            MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time + 1))
        )
        assertThat(processor.process(items)).isSameInstanceAs(items)
    }

    @Test
    fun `given a list with an item with equal timestamp as lastLoginTimestamp, it's replaced`() = runTest {
        val processor = createPostProcessor()
        val items = listOf(
            MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time))
        )
        assertThat(processor.process(items))
            .isEqualTo(listOf(MatrixTimelineItem.Virtual(encryptedHistoryBannerId, VirtualTimelineItem.EncryptedHistoryBanner)))
    }

    @Test
    fun `given a list with an item with a lower timestamp than lastLoginTimestamp, it's replaced`() = runTest {
        val processor = createPostProcessor()
        val items = listOf(
            MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time - 1))
        )
        assertThat(processor.process(items)).isEqualTo(
            listOf(MatrixTimelineItem.Virtual(encryptedHistoryBannerId, VirtualTimelineItem.EncryptedHistoryBanner))
        )
    }

    @Test
    fun `given a list with several with lower or equal timestamps than lastLoginTimestamp, then they're replaced`() = runTest {
        val processor = createPostProcessor()
        val items = listOf(
            MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time - 1)),
            MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time)),
            MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time + 1)),
        )
        assertThat(processor.process(items)).isEqualTo(
            listOf(
                MatrixTimelineItem.Virtual(encryptedHistoryBannerId, VirtualTimelineItem.EncryptedHistoryBanner),
                MatrixTimelineItem.Event(A_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time + 1))
            )
        )
    }

    private fun TestScope.createPostProcessor(
        lastLoginTimestamp: Date? = defaultLastLoginTimestamp,
        isRoomEncrypted: Boolean = true,
        isKeyBackupEnabled: Boolean = false,
    ) = TimelineEncryptedHistoryPostProcessor(
        lastLoginTimestamp = lastLoginTimestamp,
        isRoomEncrypted = isRoomEncrypted,
        isKeyBackupEnabled = isKeyBackupEnabled,
        dispatcher = StandardTestDispatcher(testScheduler)
    )
}
