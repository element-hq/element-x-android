/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

private const val FAKE_UNIQUE_ID = "FAKE_UNIQUE_ID"

class TimelineEncryptedHistoryPostProcessorTest {
    private val defaultLastLoginTimestamp = Date(1_689_061_264L)

    @Test
    fun `given an unencrypted room, nothing is done`() = runTest {
        val processor = createPostProcessor(isRoomEncrypted = false)
        val items = listOf(
            MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem())
        )
        assertThat(processor.process(items)).isSameInstanceAs(items)
    }

    @Test
    fun `given an encrypted room, and key backup enabled, nothing is done`() = runTest {
        val processor = createPostProcessor(isKeyBackupEnabled = true)
        val items = listOf(
            MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem())
        )
        assertThat(processor.process(items)).isSameInstanceAs(items)
    }

    @Test
    fun `given a null lastLoginTimestamp, nothing is done`() = runTest {
        val processor = createPostProcessor(lastLoginTimestamp = null)
        val items = listOf(
            MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem())
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
            MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time + 1))
        )
        assertThat(processor.process(items)).isSameInstanceAs(items)
    }

    @Test
    fun `given a list with an item with equal timestamp as lastLoginTimestamp, it's replaced`() = runTest {
        val processor = createPostProcessor()
        val items = listOf(
            MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time))
        )
        assertThat(processor.process(items))
            .isEqualTo(listOf(MatrixTimelineItem.Virtual(VirtualTimelineItem.EncryptedHistoryBanner.toString(), VirtualTimelineItem.EncryptedHistoryBanner)))
    }

    @Test
    fun `given a list with an item with a lower timestamp than lastLoginTimestamp, it's replaced`() = runTest {
        val processor = createPostProcessor()
        val items = listOf(
            MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time - 1))
        )
        assertThat(processor.process(items)).isEqualTo(
            listOf(MatrixTimelineItem.Virtual(VirtualTimelineItem.EncryptedHistoryBanner.toString(), VirtualTimelineItem.EncryptedHistoryBanner))
        )
    }

    @Test
    fun `given a list with several with lower or equal timestamps than lastLoginTimestamp, then they're replaced`() = runTest {
        val processor = createPostProcessor()
        val items = listOf(
            MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time - 1)),
            MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time)),
            MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time + 1)),
        )
        assertThat(processor.process(items)).isEqualTo(
            listOf(
                MatrixTimelineItem.Virtual(VirtualTimelineItem.EncryptedHistoryBanner.toString(), VirtualTimelineItem.EncryptedHistoryBanner),
                MatrixTimelineItem.Event(FAKE_UNIQUE_ID, anEventTimelineItem(timestamp = defaultLastLoginTimestamp.time + 1))
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
